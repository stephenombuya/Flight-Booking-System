package com.flightbookingapp.dto.response;

import com.flightbookingapp.model.PaymentMethod;
import com.flightbookingapp.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Payment transaction detail DTO. */
@Data
@Builder
public class PaymentResponse {
    private Long          id;
    private Long          bookingId;
    private String        bookingReference;
    private BigDecimal    amount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String        transactionId;
    private String        failureReason;
    private LocalDateTime createdAt;
}
