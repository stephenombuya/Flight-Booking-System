package com.flightbookingapp.repository;

import com.flightbookingapp.model.Flight;
import com.flightbookingapp.model.FlightStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test — spins up only the JPA layer (H2 in-memory).
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FlightRepository")
class FlightRepositoryTest {

    @Autowired FlightRepository flightRepository;

    private Flight londonToNairobi;
    private Flight nairobiToNYC;

    @BeforeEach
    void seed() {
        londonToNairobi = flightRepository.save(Flight.builder()
                .flightNumber("KQ-001")
                .airline("Kenya Airways")
                .origin("London")
                .destination("Nairobi")
                .departureTime(LocalDateTime.now().plusDays(5))
                .arrivalTime(LocalDateTime.now().plusDays(5).plusHours(9))
                .price(new BigDecimal("350.00"))
                .totalSeats(300)
                .availableSeats(250)
                .status(FlightStatus.SCHEDULED)
                .build());

        nairobiToNYC = flightRepository.save(Flight.builder()
                .flightNumber("KQ-002")
                .airline("Kenya Airways")
                .origin("Nairobi")
                .destination("New York")
                .departureTime(LocalDateTime.now().plusDays(7))
                .arrivalTime(LocalDateTime.now().plusDays(7).plusHours(14))
                .price(new BigDecimal("800.00"))
                .totalSeats(200)
                .availableSeats(50)
                .status(FlightStatus.SCHEDULED)
                .build());
    }

    @Test
    @DisplayName("existsByFlightNumber returns true for saved flight")
    void existsByFlightNumber() {
        assertThat(flightRepository.existsByFlightNumber("KQ-001")).isTrue();
        assertThat(flightRepository.existsByFlightNumber("KQ-999")).isFalse();
    }

    @Test
    @DisplayName("searchFlights with origin filter returns correct results")
    void searchByOrigin() {
        Page<Flight> result = flightRepository.searchFlights(
                "Nairobi", null, null, null,
                null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFlightNumber()).isEqualTo("KQ-002");
    }

    @Test
    @DisplayName("searchFlights with destination filter returns correct results")
    void searchByDestination() {
        Page<Flight> result = flightRepository.searchFlights(
                null, "Nairobi", null, null,
                null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFlightNumber()).isEqualTo("KQ-001");
    }

    @Test
    @DisplayName("searchFlights with price range filter returns correct results")
    void searchByPriceRange() {
        Page<Flight> result = flightRepository.searchFlights(
                null, null, null, null,
                new BigDecimal("700.00"), new BigDecimal("1000.00"), null, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFlightNumber()).isEqualTo("KQ-002");
    }

    @Test
    @DisplayName("searchFlights with minSeats filter excludes low-availability flights")
    void searchByMinSeats() {
        // Only KQ-001 has >= 200 seats available
        Page<Flight> result = flightRepository.searchFlights(
                null, null, null, null,
                null, null, null, 200,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFlightNumber()).isEqualTo("KQ-001");
    }

    @Test
    @DisplayName("searchFlights with no filters returns all flights")
    void searchNoFilters() {
        Page<Flight> result = flightRepository.searchFlights(
                null, null, null, null,
                null, null, null, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("searchFlights with status filter returns only matching flights")
    void searchByStatus() {
        londonToNairobi.setStatus(FlightStatus.CANCELLED);
        flightRepository.save(londonToNairobi);

        Page<Flight> result = flightRepository.searchFlights(
                null, null, null, null,
                null, null, FlightStatus.SCHEDULED, null,
                PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFlightNumber()).isEqualTo("KQ-002");
    }
}
