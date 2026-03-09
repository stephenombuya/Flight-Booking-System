package com.flightbookingapp.service;

import com.flightbookingapp.dto.request.PaymentRequest;
import com.flightbookingapp.dto.response.PaymentResponse;
import com.flightbookingapp.model.User;

/** Contract for payment processing. */
public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request, User currentUser);
    PaymentResponse getPaymentByBookingId(Long bookingId, User currentUser);
    PaymentResponse refundPayment(Long paymentId);
}
