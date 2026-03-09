package com.flightbookingapp;

import com.flightbookingapp.dto.request.*;
import com.flightbookingapp.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralized builder methods for test objects.
 * Avoids repeating boilerplate across test classes.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    public static User buildCustomer() {
        return User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("$2a$10$hashedpassword")
                .phone("+1234567890")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
    }

    public static User buildAdmin() {
        return User.builder()
                .id(2L)
                .firstName("Admin")
                .lastName("User")
                .email("admin@flightbooking.com")
                .password("$2a$10$hashedpassword")
                .phone("+0987654321")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
    }

    public static Flight buildFlight() {
        return Flight.builder()
                .id(1L)
                .flightNumber("FBS-001")
                .airline("Demo Air")
                .origin("Nairobi")
                .destination("London")
                .departureTime(LocalDateTime.now().plusDays(10))
                .arrivalTime(LocalDateTime.now().plusDays(10).plusHours(8))
                .price(new BigDecimal("450.00"))
                .totalSeats(200)
                .availableSeats(180)
                .status(FlightStatus.SCHEDULED)
                .build();
    }

    public static Booking buildBooking(User user, Flight flight) {
        return Booking.builder()
                .id(1L)
                .bookingReference("FBS-20240101-00001")
                .user(user)
                .flight(flight)
                .numberOfPassengers(2)
                .totalAmount(new BigDecimal("900.00"))
                .status(BookingStatus.PENDING)
                .passengerNames(List.of("Jane Doe", "John Doe"))
                .build();
    }

    public static Payment buildPayment(Booking booking) {
        return Payment.builder()
                .id(1L)
                .booking(booking)
                .amount(booking.getTotalAmount())
                .paymentStatus(PaymentStatus.COMPLETED)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .transactionId("TXN-ABC123")
                .build();
    }

    public static RegisterRequest buildRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jane");
        req.setLastName("Doe");
        req.setEmail("jane.doe@example.com");
        req.setPassword("Password1");
        req.setPhone("+1234567890");
        return req;
    }

    public static LoginRequest buildLoginRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("jane.doe@example.com");
        req.setPassword("Password1");
        return req;
    }

    public static FlightRequest buildFlightRequest() {
        FlightRequest req = new FlightRequest();
        req.setFlightNumber("FBS-001");
        req.setAirline("Demo Air");
        req.setOrigin("Nairobi");
        req.setDestination("London");
        req.setDepartureTime(LocalDateTime.now().plusDays(10));
        req.setArrivalTime(LocalDateTime.now().plusDays(10).plusHours(8));
        req.setPrice(new BigDecimal("450.00"));
        req.setTotalSeats(200);
        return req;
    }

    public static BookingRequest buildBookingRequest(Long flightId) {
        BookingRequest req = new BookingRequest();
        req.setFlightId(flightId);
        req.setNumberOfPassengers(2);
        req.setPassengerNames(List.of("Jane Doe", "John Doe"));
        return req;
    }

    public static PaymentRequest buildPaymentRequest(Long bookingId) {
        PaymentRequest req = new PaymentRequest();
        req.setBookingId(bookingId);
        req.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        req.setPaymentToken("VALID_TOKEN");
        return req;
    }
}
