package com.flightbookingapp.service;

import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.BookingRequest;
import com.flightbookingapp.dto.response.BookingResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.exception.UnauthorizedException;
import com.flightbookingapp.model.*;
import com.flightbookingapp.repository.BookingRepository;
import com.flightbookingapp.repository.FlightRepository;
import com.flightbookingapp.service.impl.BookingServiceImpl;
import com.flightbookingapp.util.BookingReferenceGenerator;
import com.flightbookingapp.util.EmailService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService")
class BookingServiceTest {

    @Mock private BookingRepository         bookingRepository;
    @Mock private FlightRepository          flightRepository;
    @Mock private BookingReferenceGenerator referenceGenerator;
    @Mock private EmailService              emailService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User customer;
    private User admin;
    private Flight flight;

    @BeforeEach
    void setUp() {
        customer = TestDataFactory.buildCustomer();
        admin    = TestDataFactory.buildAdmin();
        flight   = TestDataFactory.buildFlight();
    }

    // ---- createBooking ----

    @Test
    @DisplayName("createBooking should reserve seats and return response")
    void createBooking_success() {
        BookingRequest request = TestDataFactory.buildBookingRequest(flight.getId());
        Booking booking = TestDataFactory.buildBooking(customer, flight);

        when(flightRepository.findById(flight.getId())).thenReturn(Optional.of(flight));
        when(referenceGenerator.generate()).thenReturn("FBS-20240101-00001");
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.createBooking(request, customer);

        assertThat(response.getBookingReference()).isEqualTo("FBS-20240101-00001");
        assertThat(response.getNumberOfPassengers()).isEqualTo(2);
        // seats should have been decremented
        verify(flightRepository).save(argThat(f -> f.getAvailableSeats() == 178));
    }

    @Test
    @DisplayName("createBooking should throw BadRequestException if not enough seats")
    void createBooking_notEnoughSeats() {
        flight.setAvailableSeats(1);
        BookingRequest request = TestDataFactory.buildBookingRequest(flight.getId());
        request.setNumberOfPassengers(2);
        request.getPassengerNames().set(0, "Jane");

        when(flightRepository.findById(flight.getId())).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("seats available");
    }

    @Test
    @DisplayName("createBooking should throw BadRequestException for cancelled flight")
    void createBooking_cancelledFlight() {
        flight.setStatus(FlightStatus.CANCELLED);
        BookingRequest request = TestDataFactory.buildBookingRequest(flight.getId());

        when(flightRepository.findById(flight.getId())).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> bookingService.createBooking(request, customer))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cancelled");
    }

    // ---- cancelBooking ----

    @Test
    @DisplayName("cancelBooking should release seats and send email")
    void cancelBooking_success() {
        Booking booking = TestDataFactory.buildBooking(customer, flight);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        bookingService.cancelBooking(1L, customer);

        verify(flightRepository).save(argThat(f -> f.getAvailableSeats() == 182));
        verify(emailService).sendBookingCancellation(booking);
    }

    @Test
    @DisplayName("cancelBooking should throw UnauthorizedException for different user")
    void cancelBooking_unauthorizedUser() {
        User other = User.builder().id(99L).role(Role.CUSTOMER).build();
        Booking booking = TestDataFactory.buildBooking(customer, flight);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, other))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("cancelBooking should throw BadRequestException for completed booking")
    void cancelBooking_completed() {
        Booking booking = TestDataFactory.buildBooking(customer, flight);
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(1L, customer))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Completed");
    }

    // ---- confirmBooking ----

    @Test
    @DisplayName("confirmBooking should update status to CONFIRMED")
    void confirmBooking_success() {
        Booking booking = TestDataFactory.buildBooking(customer, flight);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(emailService).sendBookingConfirmation(any());

        BookingResponse response = bookingService.confirmBooking(1L);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirmBooking should throw for non-pending booking")
    void confirmBooking_notPending() {
        Booking booking = TestDataFactory.buildBooking(customer, flight);
        booking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("getBookingById should throw ResourceNotFoundException for unknown ID")
    void getBookingById_notFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L, customer))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("admin can access any user's booking")
    void getBookingById_adminCanAccess() {
        Booking booking = TestDataFactory.buildBooking(customer, flight);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatCode(() -> bookingService.getBookingById(1L, admin))
                .doesNotThrowAnyException();
    }
}
