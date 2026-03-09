package com.flightbookingapp.dto.request;

import com.flightbookingapp.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Payload to initiate payment for a confirmed booking. */
@Data
public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /** Simulated card / account token from the frontend. */
    private String paymentToken;
}
