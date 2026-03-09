package com.flightbookingapp.service;

import com.flightbookingapp.dto.request.FlightRequest;
import com.flightbookingapp.dto.request.FlightSearchRequest;
import com.flightbookingapp.dto.response.FlightResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

/** Contract for flight management and search. */
public interface FlightService {
    FlightResponse createFlight(FlightRequest request);
    FlightResponse updateFlight(Long id, FlightRequest request);
    void deleteFlight(Long id);
    FlightResponse getFlightById(Long id);
    PagedResponse<FlightResponse> searchFlights(FlightSearchRequest searchRequest, Pageable pageable);
    PagedResponse<FlightResponse> getAllFlights(Pageable pageable);
}
