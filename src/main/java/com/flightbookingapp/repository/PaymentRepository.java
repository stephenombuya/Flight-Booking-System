package com.flightbookingapp.repository;

import com.flightbookingapp.model.Payment;
import com.flightbookingapp.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for payment transaction records.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByBookingId(Long bookingId);

    long countByPaymentStatus(PaymentStatus status);
}
