package com.flightbookingapp.controller;

import com.flightbookingapp.dto.request.FlightRequest;
import com.flightbookingapp.dto.request.FlightSearchRequest;
import com.flightbookingapp.dto.response.ApiResponse;
import com.flightbookingapp.dto.response.FlightResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Public flight search (GET) and admin flight management (POST/PUT/DELETE).
 */
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight search and management")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Search flights with optional filters (public)")
    public ApiResponse<PagedResponse<FlightResponse>> searchFlights(
            @ParameterObject FlightSearchRequest searchRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "departureTime",
                                               direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.success(flightService.searchFlights(searchRequest, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a flight by ID (public)")
    public ApiResponse<FlightResponse> getFlightById(@PathVariable Long id) {
        return ApiResponse.success(flightService.getFlightById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new flight (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<FlightResponse> createFlight(@Valid @RequestBody FlightRequest request) {
        return ApiResponse.success("Flight created", flightService.createFlight(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing flight (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<FlightResponse> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody FlightRequest request) {
        return ApiResponse.success("Flight updated", flightService.updateFlight(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a flight (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    public void deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
    }
}
