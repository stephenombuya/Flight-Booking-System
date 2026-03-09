package com.flightbookingapp.repository;

import com.flightbookingapp.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BookingRepository")
class BookingRepositoryTest {

    @Autowired BookingRepository bookingRepository;
    @Autowired UserRepository    userRepository;
    @Autowired FlightRepository  flightRepository;

    private User user;
    private Flight flight;

    @BeforeEach
    void seed() {
        user = userRepository.save(User.builder()
                .firstName("Jane").lastName("Doe").email("jane@test.com")
                .password("hashed").phone("+1234567890")
                .role(Role.CUSTOMER).enabled(true).build());

        flight = flightRepository.save(Flight.builder()
                .flightNumber("TEST-001").airline("Test Air")
                .origin("A").destination("B")
                .departureTime(LocalDateTime.now().plusDays(3))
                .arrivalTime(LocalDateTime.now().plusDays(3).plusHours(2))
                .price(new BigDecimal("100.00"))
                .totalSeats(100).availableSeats(80)
                .status(FlightStatus.SCHEDULED).build());

        bookingRepository.save(Booking.builder()
                .bookingReference("REF-001").user(user).flight(flight)
                .numberOfPassengers(2).totalAmount(new BigDecimal("200.00"))
                .status(BookingStatus.CONFIRMED)
                .passengerNames(List.of("Jane Doe", "John Doe")).build());

        bookingRepository.save(Booking.builder()
                .bookingReference("REF-002").user(user).flight(flight)
                .numberOfPassengers(1).totalAmount(new BigDecimal("100.00"))
                .status(BookingStatus.CANCELLED)
                .passengerNames(List.of("Jane Doe")).build());
    }

    @Test
    @DisplayName("findByBookingReference returns correct booking")
    void findByReference() {
        assertThat(bookingRepository.findByBookingReference("REF-001")).isPresent();
        assertThat(bookingRepository.findByBookingReference("UNKNOWN")).isEmpty();
    }

    @Test
    @DisplayName("countByStatus returns correct counts")
    void countByStatus() {
        assertThat(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).isEqualTo(1);
        assertThat(bookingRepository.countByStatus(BookingStatus.CANCELLED)).isEqualTo(1);
        assertThat(bookingRepository.countByStatus(BookingStatus.PENDING)).isEqualTo(0);
    }

    @Test
    @DisplayName("calculateRevenue sums confirmed booking amounts")
    void calculateRevenue() {
        BigDecimal revenue = bookingRepository.calculateRevenue(
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // Only REF-001 is CONFIRMED ($200)
        assertThat(revenue).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("findByUserId returns paginated bookings for user")
    void findByUserId() {
        var page = bookingRepository.findByUserId(
                user.getId(), org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }
}
