package com.flightbookingapp.controller;

import com.flightbookingapp.dto.request.BookingRequest;
import com.flightbookingapp.dto.response.ApiResponse;
import com.flightbookingapp.dto.response.BookingResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.model.User;
import com.flightbookingapp.service.BookingService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Booking creation, retrieval, and cancellation for authenticated users.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Flight reservation management")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new flight booking")
    public ApiResponse<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success("Booking created", bookingService.createBooking(request, currentUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details by ID")
    public ApiResponse<BookingResponse> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(bookingService.getBookingById(id, currentUser));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Look up a booking by its reference code")
    public ApiResponse<BookingResponse> getByReference(
            @PathVariable String reference,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(bookingService.getBookingByReference(reference, currentUser));
    }

    @GetMapping("/my")
    @Operation(summary = "List all bookings for the currently authenticated user")
    public ApiResponse<PagedResponse<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal User currentUser,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt",
                                               direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(
                bookingService.getUserBookings(currentUser.getId(), currentUser, pageable));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success("Booking cancelled",
                bookingService.cancelBooking(id, currentUser));
    }
}
