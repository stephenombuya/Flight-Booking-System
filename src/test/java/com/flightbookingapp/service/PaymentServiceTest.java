package com.flightbookingapp.service;

import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.PaymentRequest;
import com.flightbookingapp.dto.response.PaymentResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ConflictException;
import com.flightbookingapp.exception.PaymentException;
import com.flightbookingapp.model.*;
import com.flightbookingapp.repository.BookingRepository;
import com.flightbookingapp.repository.PaymentRepository;
import com.flightbookingapp.service.impl.PaymentServiceImpl;
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
@DisplayName("PaymentService")
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookingService    bookingService;
    @Mock private EmailService      emailService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User customer;
    private Flight flight;
    private Booking booking;

    @BeforeEach
    void setUp() {
        customer = TestDataFactory.buildCustomer();
        flight   = TestDataFactory.buildFlight();
        booking  = TestDataFactory.buildBooking(customer, flight);
    }

    @Test
    @DisplayName("processPayment should complete payment and confirm booking")
    void processPayment_success() {
        PaymentRequest request = TestDataFactory.buildPaymentRequest(booking.getId());

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingId(booking.getId())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> {
                    Payment p = inv.getArgument(0);
                    p.setId(1L);
                    return p;
                });
        when(bookingService.confirmBooking(booking.getId()))
                .thenReturn(null); // return value not used here

        PaymentResponse response = paymentService.processPayment(request, customer);

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.getTransactionId()).isNotBlank();
        verify(bookingService).confirmBooking(booking.getId());
    }

    @Test
    @DisplayName("processPayment should throw PaymentException for FAIL_ token")
    void processPayment_gatewayDecline() {
        PaymentRequest request = TestDataFactory.buildPaymentRequest(booking.getId());
        request.setPaymentToken("FAIL_card_declined");

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingId(booking.getId())).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> paymentService.processPayment(request, customer))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Payment failed");
    }

    @Test
    @DisplayName("processPayment should throw ConflictException if payment already exists")
    void processPayment_alreadyPaid() {
        PaymentRequest request = TestDataFactory.buildPaymentRequest(booking.getId());

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingId(booking.getId())).thenReturn(true);

        assertThatThrownBy(() -> paymentService.processPayment(request, customer))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("processPayment should throw BadRequestException for non-PENDING booking")
    void processPayment_bookingNotPending() {
        booking.setStatus(BookingStatus.CONFIRMED);
        PaymentRequest request = TestDataFactory.buildPaymentRequest(booking.getId());

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.processPayment(request, customer))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("refundPayment should change status to REFUNDED")
    void refundPayment_success() {
        Payment payment = TestDataFactory.buildPayment(booking);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.refundPayment(1L);

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    @DisplayName("refundPayment should throw BadRequestException for non-COMPLETED payment")
    void refundPayment_notCompleted() {
        Payment payment = TestDataFactory.buildPayment(booking);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(BadRequestException.class);
    }
}
