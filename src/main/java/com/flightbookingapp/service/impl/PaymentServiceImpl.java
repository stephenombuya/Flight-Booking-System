package com.flightbookingapp.service.impl;

import com.flightbookingapp.dto.request.PaymentRequest;
import com.flightbookingapp.dto.response.PaymentResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ConflictException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.exception.UnauthorizedException;
import com.flightbookingapp.model.*;
import com.flightbookingapp.repository.BookingRepository;
import com.flightbookingapp.repository.PaymentRepository;
import com.flightbookingapp.service.BookingService;
import com.flightbookingapp.service.PaymentService;
import com.flightbookingapp.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Simulates payment processing and manages the Payment entity lifecycle.
 *
 * <p>In a real system the gateway call (Stripe, PayPal, etc.) would replace
 * the stub in {@code simulateGatewayCall()}. The transaction boundary ensures
 * the booking status update and payment record are committed atomically.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository  paymentRepository;
    private final BookingRepository  bookingRepository;
    private final BookingService     bookingService;
    private final EmailService       emailService;

    @Override
    public PaymentResponse processPayment(PaymentRequest request, User currentUser) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        // Ownership check
        if (!booking.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorised to pay for this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING bookings can be paid; current status: " + booking.getStatus());
        }

        if (paymentRepository.existsByBookingId(booking.getId())) {
            throw new ConflictException("A payment record already exists for booking " +
                    booking.getBookingReference());
        }

        // Build a PENDING payment record first
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // Simulate gateway
        GatewayResult result = simulateGatewayCall(request.getPaymentToken());

        if (result.success()) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(result.transactionId());
            Payment saved = paymentRepository.save(payment);

            // Confirm the booking
            bookingService.confirmBooking(booking.getId());

            emailService.sendPaymentReceipt(saved);
            log.info("Payment completed: txn={} booking={}", result.transactionId(),
                     booking.getBookingReference());
            return toResponse(saved);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
            paymentRepository.save(payment);
            log.warn("Payment failed for booking {}: {}", booking.getBookingReference(),
                     result.failureReason());
            throw new com.flightbookingapp.exception.PaymentException(
                    "Payment failed: " + result.failureReason());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByBookingId(Long bookingId, User currentUser) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));

        Booking booking = payment.getBooking();
        if (!booking.getUser().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Access denied");
        }

        return toResponse(payment);
    }

    @Override
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only COMPLETED payments can be refunded");
        }

        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment refunded: id={}", paymentId);
        return toResponse(saved);
    }

    // ---- Gateway stub ----

    /**
     * Stub for an external payment gateway.
     * Replace with a real Stripe/PayPal SDK call in production.
     * Returns failure for tokens starting with "FAIL_" to support testing.
     */
    private GatewayResult simulateGatewayCall(String token) {
        if (token != null && token.startsWith("FAIL_")) {
            return new GatewayResult(false, null, "Card declined");
        }
        return new GatewayResult(true, "TXN-" + UUID.randomUUID().toString().toUpperCase(), null);
    }

    private record GatewayResult(boolean success, String transactionId, String failureReason) {}

    // ---- Mapper ----

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .bookingId(p.getBooking().getId())
                .bookingReference(p.getBooking().getBookingReference())
                .amount(p.getAmount())
                .paymentStatus(p.getPaymentStatus())
                .paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
