package com.flightbookingapp.controller;

import com.flightbookingapp.dto.request.PaymentRequest;
import com.flightbookingapp.dto.response.ApiResponse;
import com.flightbookingapp.dto.response.PaymentResponse;
import com.flightbookingapp.model.User;
import com.flightbookingapp.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Payment processing and retrieval endpoints.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Process payment for a pending booking")
    public ApiResponse<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success("Payment processed",
                paymentService.processPayment(request, currentUser));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment details for a booking")
    public ApiResponse<PaymentResponse> getPaymentByBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(paymentService.getPaymentByBookingId(bookingId, currentUser));
    }
}
