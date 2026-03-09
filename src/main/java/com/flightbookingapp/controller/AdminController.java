package com.flightbookingapp.controller;

import com.flightbookingapp.dto.response.*;
import com.flightbookingapp.model.User;
import com.flightbookingapp.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Admin-only endpoints for managing users, bookings, payments, and reports.
 * All routes require the ADMIN role enforced by both Security config and @PreAuthorize.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard and management operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService   adminService;
    private final UserService    userService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    // ---- Dashboard ----

    @GetMapping("/dashboard")
    @Operation(summary = "Get aggregated system statistics")
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.success(adminService.getDashboardStats());
    }

    @GetMapping("/revenue")
    @Operation(summary = "Get total confirmed revenue between two dates")
    public ApiResponse<BigDecimal> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.success(adminService.getRevenueBetween(from, to));
    }

    // ---- User management ----

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated)")
    public ApiResponse<PagedResponse<UserResponse>> getAllUsers(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                                               direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(userService.getAllUsers(pageable));
    }

    @PatchMapping("/users/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Disable a user account")
    public void disableUser(@PathVariable Long id) {
        userService.disableUser(id);
    }

    // ---- Booking management ----

    @GetMapping("/bookings")
    @Operation(summary = "List all bookings (paginated)")
    public ApiResponse<PagedResponse<BookingResponse>> getAllBookings(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                                               direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(bookingService.getAllBookings(pageable));
    }

    @PatchMapping("/bookings/{id}/confirm")
    @Operation(summary = "Manually confirm a pending booking")
    public ApiResponse<BookingResponse> confirmBooking(@PathVariable Long id) {
        return ApiResponse.success("Booking confirmed", bookingService.confirmBooking(id));
    }

    @PatchMapping("/bookings/{id}/cancel")
    @Operation(summary = "Cancel any booking (admin override)")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long id,
            org.springframework.security.core.annotation.AuthenticationPrincipal User admin) {
        return ApiResponse.success("Booking cancelled", bookingService.cancelBooking(id, admin));
    }

    // ---- Payment management ----

    @PatchMapping("/payments/{id}/refund")
    @Operation(summary = "Issue a refund for a completed payment")
    public ApiResponse<PaymentResponse> refundPayment(@PathVariable Long id) {
        return ApiResponse.success("Payment refunded", paymentService.refundPayment(id));
    }
}
