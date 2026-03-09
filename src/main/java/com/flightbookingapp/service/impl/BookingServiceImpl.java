package com.flightbookingapp.service.impl;

import com.flightbookingapp.dto.request.BookingRequest;
import com.flightbookingapp.dto.response.BookingResponse;
import com.flightbookingapp.dto.response.FlightResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.dto.response.UserResponse;
import com.flightbookingapp.exception.BadRequestException;
import com.flightbookingapp.exception.ResourceNotFoundException;
import com.flightbookingapp.exception.UnauthorizedException;
import com.flightbookingapp.model.*;
import com.flightbookingapp.repository.BookingRepository;
import com.flightbookingapp.repository.FlightRepository;
import com.flightbookingapp.service.BookingService;
import com.flightbookingapp.util.BookingReferenceGenerator;
import com.flightbookingapp.util.EmailService;
import com.flightbookingapp.util.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Manages the full booking lifecycle: creation, confirmation, and cancellation.
 * Uses optimistic seat-reservation within a transaction to prevent overselling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository         bookingRepository;
    private final FlightRepository          flightRepository;
    private final BookingReferenceGenerator referenceGenerator;
    private final EmailService              emailService;

    @Override
    public BookingResponse createBooking(BookingRequest request, User currentUser) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", request.getFlightId()));

        validateBookingRequest(request, flight);

        // Reserve seats atomically
        flight.setAvailableSeats(flight.getAvailableSeats() - request.getNumberOfPassengers());
        flightRepository.save(flight);

        BigDecimal total = flight.getPrice()
                .multiply(BigDecimal.valueOf(request.getNumberOfPassengers()));

        Booking booking = Booking.builder()
                .bookingReference(referenceGenerator.generate())
                .user(currentUser)
                .flight(flight)
                .numberOfPassengers(request.getNumberOfPassengers())
                .totalAmount(total)
                .status(BookingStatus.PENDING)
                .passengerNames(request.getPassengerNames())
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: {} for user {}", saved.getBookingReference(), currentUser.getEmail());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, User currentUser) {
        Booking booking = findOrThrow(id);
        assertOwnerOrAdmin(booking, currentUser);
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference, User currentUser) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", reference));
        assertOwnerOrAdmin(booking, currentUser);
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getUserBookings(Long userId, User currentUser, Pageable pageable) {
        // Customers can only see their own bookings
        if (currentUser.getRole() != Role.ADMIN &&
                !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }
        return PageUtils.toPagedResponse(
                bookingRepository.findByUserId(userId, pageable),
                this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookings(Pageable pageable) {
        return PageUtils.toPagedResponse(bookingRepository.findAll(pageable), this::toResponse);
    }

    @Override
    public BookingResponse confirmBooking(Long id) {
        Booking booking = findOrThrow(id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException(
                    "Only PENDING bookings can be confirmed; current status: " + booking.getStatus());
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        emailService.sendBookingConfirmation(saved);
        log.info("Booking confirmed: {}", saved.getBookingReference());
        return toResponse(saved);
    }

    @Override
    public BookingResponse cancelBooking(Long id, User currentUser) {
        Booking booking = findOrThrow(id);
        assertOwnerOrAdmin(booking, currentUser);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled");
        }

        // Release seats back to the flight
        Flight flight = booking.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + booking.getNumberOfPassengers());
        flightRepository.save(flight);

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        emailService.sendBookingCancellation(saved);
        log.info("Booking cancelled: {}", saved.getBookingReference());
        return toResponse(saved);
    }

    // ---- Helpers ----

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    private void validateBookingRequest(BookingRequest request, Flight flight) {
        if (flight.getStatus() == FlightStatus.CANCELLED) {
            throw new BadRequestException("Cannot book a cancelled flight");
        }
        if (flight.getAvailableSeats() < request.getNumberOfPassengers()) {
            throw new BadRequestException(
                    "Not enough seats available. Requested: " + request.getNumberOfPassengers() +
                    ", Available: " + flight.getAvailableSeats());
        }
        if (request.getPassengerNames().size() != request.getNumberOfPassengers()) {
            throw new BadRequestException(
                    "Number of passenger names must match number of passengers");
        }
    }

    private void assertOwnerOrAdmin(Booking booking, User currentUser) {
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You are not authorised to access this booking");
        }
    }

    private FlightResponse toFlightResponse(Flight f) {
        long minutes = Duration.between(f.getDepartureTime(), f.getArrivalTime()).toMinutes();
        return FlightResponse.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .airline(f.getAirline())
                .origin(f.getOrigin())
                .destination(f.getDestination())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .price(f.getPrice())
                .totalSeats(f.getTotalSeats())
                .availableSeats(f.getAvailableSeats())
                .status(f.getStatus())
                .durationMinutes(minutes)
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private BookingResponse toResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .bookingReference(b.getBookingReference())
                .user(toUserResponse(b.getUser()))
                .flight(toFlightResponse(b.getFlight()))
                .numberOfPassengers(b.getNumberOfPassengers())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus())
                .passengerNames(b.getPassengerNames())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
