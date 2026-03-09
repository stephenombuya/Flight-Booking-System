package com.flightbookingapp.service.impl;

import com.flightbookingapp.dto.request.FlightRequest;
import com.flightbookingapp.dto.request.FlightSearchRequest;
import com.flightbookingapp.dto.response.FlightResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ConflictException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.model.Flight;
import com.flightbookingapp.model.FlightStatus;
import com.flightbookingapp.repository.FlightRepository;
import com.flightbookingapp.service.FlightService;
import com.flightbookingapp.util.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Flight CRUD and search operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    public FlightResponse createFlight(FlightRequest request) {
        if (flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new ConflictException(
                    "Flight number '" + request.getFlightNumber() + "' already exists");
        }
        validateTimes(request);

        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .airline(request.getAirline())
                .origin(request.getOrigin())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .price(request.getPrice())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .status(request.getStatus() != null ? request.getStatus() : FlightStatus.SCHEDULED)
                .build();

        Flight saved = flightRepository.save(flight);
        log.info("Flight created: {} (id={})", saved.getFlightNumber(), saved.getId());
        return toResponse(saved);
    }

    @Override
    public FlightResponse updateFlight(Long id, FlightRequest request) {
        Flight flight = findOrThrow(id);
        validateTimes(request);

        // If flight number changed, check it's not already taken
        if (!flight.getFlightNumber().equals(request.getFlightNumber()) &&
                flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new ConflictException(
                    "Flight number '" + request.getFlightNumber() + "' already in use");
        }

        int seatDiff = request.getTotalSeats() - flight.getTotalSeats();

        flight.setFlightNumber(request.getFlightNumber());
        flight.setAirline(request.getAirline());
        flight.setOrigin(request.getOrigin());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setPrice(request.getPrice());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(Math.max(0, flight.getAvailableSeats() + seatDiff));

        if (request.getStatus() != null) flight.setStatus(request.getStatus());

        return toResponse(flightRepository.save(flight));
    }

    @Override
    public void deleteFlight(Long id) {
        Flight flight = findOrThrow(id);
        flightRepository.delete(flight);
        log.info("Flight deleted: {} (id={})", flight.getFlightNumber(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FlightResponse> searchFlights(FlightSearchRequest req, Pageable pageable) {
        return PageUtils.toPagedResponse(
                flightRepository.searchFlights(
                        req.getOrigin(), req.getDestination(),
                        req.getFromDate(), req.getToDate(),
                        req.getMinPrice(), req.getMaxPrice(),
                        req.getStatus(), req.getMinSeats(),
                        pageable),
                this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FlightResponse> getAllFlights(Pageable pageable) {
        return PageUtils.toPagedResponse(flightRepository.findAll(pageable), this::toResponse);
    }

    // ---- Helpers ----

    private Flight findOrThrow(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", id));
    }

    private void validateTimes(FlightRequest request) {
        if (request.getArrivalTime() != null &&
                request.getDepartureTime() != null &&
                !request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new BadRequestException("Arrival time must be after departure time");
        }
    }

    private FlightResponse toResponse(Flight f) {
        long minutes = Duration.between(f.getDepartureTime(), f.getArrivalTime()).toMinutes();
        return FlightResponse.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .airline(f.getAirline())
                .origin(f.getOrigin())
                .destination(f.getDestination())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .price(f.getPrice())
                .totalSeats(f.getTotalSeats())
                .availableSeats(f.getAvailableSeats())
                .status(f.getStatus())
                .durationMinutes(minutes)
                .build();
    }
}
