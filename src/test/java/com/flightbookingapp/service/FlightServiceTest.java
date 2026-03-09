package com.flightbookingapp.service;

import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.FlightRequest;
import com.flightbookingapp.dto.response.FlightResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ConflictException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.model.Flight;
import com.flightbookingapp.repository.FlightRepository;
import com.flightbookingapp.service.impl.FlightServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightService")
class FlightServiceTest {

    @Mock private FlightRepository flightRepository;
    @InjectMocks private FlightServiceImpl flightService;

    @Test
    @DisplayName("createFlight should persist and return FlightResponse")
    void createFlight_success() {
        FlightRequest request = TestDataFactory.buildFlightRequest();
        Flight flight = TestDataFactory.buildFlight();

        when(flightRepository.existsByFlightNumber(request.getFlightNumber())).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        FlightResponse response = flightService.createFlight(request);

        assertThat(response.getFlightNumber()).isEqualTo(flight.getFlightNumber());
        assertThat(response.getOrigin()).isEqualTo("Nairobi");
        assertThat(response.getAvailableSeats()).isEqualTo(180);
    }

    @Test
    @DisplayName("createFlight should throw ConflictException for duplicate flight number")
    void createFlight_duplicateNumber() {
        FlightRequest request = TestDataFactory.buildFlightRequest();
        when(flightRepository.existsByFlightNumber(request.getFlightNumber())).thenReturn(true);

        assertThatThrownBy(() -> flightService.createFlight(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("createFlight should throw BadRequestException when arrival <= departure")
    void createFlight_invalidTimes() {
        FlightRequest request = TestDataFactory.buildFlightRequest();
        request.setArrivalTime(request.getDepartureTime().minusHours(1));

        when(flightRepository.existsByFlightNumber(any())).thenReturn(false);

        assertThatThrownBy(() -> flightService.createFlight(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Arrival time");
    }

    @Test
    @DisplayName("getFlightById should throw ResourceNotFoundException for unknown ID")
    void getFlightById_notFound() {
        when(flightRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteFlight should call repository.delete")
    void deleteFlight_success() {
        Flight flight = TestDataFactory.buildFlight();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        flightService.deleteFlight(1L);

        verify(flightRepository).delete(flight);
    }

    @Test
    @DisplayName("updateFlight adjusts availableSeats proportionally when totalSeats changes")
    void updateFlight_adjustsAvailableSeats() {
        Flight existing = TestDataFactory.buildFlight(); // 200 total, 180 available (20 booked)
        FlightRequest request = TestDataFactory.buildFlightRequest();
        request.setTotalSeats(250); // adding 50 seats

        when(flightRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(flightRepository.existsByFlightNumber(any())).thenReturn(false);
        when(flightRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FlightResponse response = flightService.updateFlight(1L, request);

        // 180 + 50 = 230
        assertThat(response.getAvailableSeats()).isEqualTo(230);
        assertThat(response.getTotalSeats()).isEqualTo(250);
    }
}
