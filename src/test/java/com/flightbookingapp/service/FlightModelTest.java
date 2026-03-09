package com.flightbookingapp.service;

import com.flightbookingapp.model.Flight;
import com.flightbookingapp.model.FlightClass;
import com.flightbookingapp.model.FlightStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Flight Model Unit Tests")
class FlightModelTest {

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.builder()
                .id(1L)
                .flightNumber("KQ100")
                .airline("Kenya Airways")
                .departureAirport("NBO")
                .arrivalAirport("LHR")
                .departureCity("Nairobi")
                .arrivalCity("London")
                .departureTime(LocalDateTime.of(2025, 6, 1, 22, 0))
                .arrivalTime(LocalDateTime.of(2025, 6, 2, 8, 0))
                .basePrice(BigDecimal.valueOf(600))
                .totalSeats(100)
                .availableSeats(50)
                .flightClass(FlightClass.ECONOMY)
                .status(FlightStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("getDurationMinutes() returns correct minutes between departure and arrival")
    void getDurationMinutes_isCorrect() {
        assertThat(flight.getDurationMinutes()).isEqualTo(600); // 10 hours
    }

    @Test
    @DisplayName("hasAvailableSeats() returns true when enough seats are available")
    void hasAvailableSeats_sufficient() {
        assertThat(flight.hasAvailableSeats(50)).isTrue();
        assertThat(flight.hasAvailableSeats(1)).isTrue();
    }

    @Test
    @DisplayName("hasAvailableSeats() returns false when not enough seats")
    void hasAvailableSeats_insufficient() {
        assertThat(flight.hasAvailableSeats(51)).isFalse();
    }

    @Test
    @DisplayName("reserveSeats() reduces availableSeats by the given count")
    void reserveSeats_reducesCount() {
        flight.reserveSeats(10);
        assertThat(flight.getAvailableSeats()).isEqualTo(40);
    }

    @Test
    @DisplayName("reserveSeats() throws when not enough seats")
    void reserveSeats_notEnough_throws() {
        assertThatThrownBy(() -> flight.reserveSeats(51))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough available seats");
    }

    @Test
    @DisplayName("releaseSeats() increases availableSeats but does not exceed totalSeats")
    void releaseSeats_cappedAtTotal() {
        flight.releaseSeats(60); // 50 + 60 > 100
        assertThat(flight.getAvailableSeats()).isEqualTo(100);
    }
}
