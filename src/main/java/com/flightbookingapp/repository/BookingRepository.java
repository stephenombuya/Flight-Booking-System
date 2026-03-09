package com.flightbookingapp.repository;

import com.flightbookingapp.model.Booking;
import com.flightbookingapp.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for booking retrieval and admin reporting queries.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByFlightId(Long flightId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    boolean existsByBookingReference(String bookingReference);

    /** Revenue report: total confirmed revenue within a date range. */
    @Query("""
            SELECT COALESCE(SUM(b.totalAmount), 0)
            FROM Booking b
            WHERE b.status = 'CONFIRMED'
              AND b.createdAt BETWEEN :from AND :to
            """)
    java.math.BigDecimal calculateRevenue(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    /** Count bookings by status (used for admin dashboard). */
    long countByStatus(BookingStatus status);
}
