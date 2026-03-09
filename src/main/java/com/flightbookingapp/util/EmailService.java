package com.flightbookingapp.util;

import com.flightbookingapp.model.Booking;
import com.flightbookingapp.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails for booking confirmations, cancellations,
 * and payment receipts.
 *
 * <p>All methods are {@code @Async} so email delivery never blocks the HTTP
 * response thread.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        String subject = "Booking Confirmed – " + booking.getBookingReference();
        String body = String.format("""
                Dear %s,

                Your booking has been confirmed!

                Reference : %s
                Flight    : %s → %s (%s)
                Departure : %s
                Passengers: %d
                Total     : $%.2f

                Thank you for choosing Flight Booking System.
                """,
                booking.getUser().getFirstName(),
                booking.getBookingReference(),
                booking.getFlight().getOrigin(),
                booking.getFlight().getDestination(),
                booking.getFlight().getFlightNumber(),
                booking.getFlight().getDepartureTime(),
                booking.getNumberOfPassengers(),
                booking.getTotalAmount());

        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    @Async
    public void sendBookingCancellation(Booking booking) {
        String subject = "Booking Cancelled – " + booking.getBookingReference();
        String body = String.format("""
                Dear %s,

                Your booking %s has been cancelled.

                If you believe this is an error, please contact support.

                Thank you,
                Flight Booking System
                """,
                booking.getUser().getFirstName(),
                booking.getBookingReference());

        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    @Async
    public void sendPaymentReceipt(Payment payment) {
        Booking booking = payment.getBooking();
        String subject = "Payment Receipt – " + payment.getTransactionId();
        String body = String.format("""
                Dear %s,

                Payment received!

                Transaction ID : %s
                Booking Ref    : %s
                Amount         : $%.2f
                Method         : %s

                Thank you,
                Flight Booking System
                """,
                booking.getUser().getFirstName(),
                payment.getTransactionId(),
                booking.getBookingReference(),
                payment.getAmount(),
                payment.getPaymentMethod());

        sendEmail(booking.getUser().getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
