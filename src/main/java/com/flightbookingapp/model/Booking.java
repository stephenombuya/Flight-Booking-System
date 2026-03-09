package com.flightbookingapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A flight reservation made by a user.
 * Holds the number of passengers, total charge, and lifecycle status.
 */
@Entity
@Table(name = "bookings",
        indexes = {
            @Index(name = "idx_booking_user",   columnList = "user_id"),
            @Index(name = "idx_booking_flight", columnList = "flight_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique reference code shown to customers (e.g. FBS-20240101-00042). */
    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    @Column(nullable = false)
    private Integer numberOfPassengers;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    /** JSON-serialisable list of passenger names stored as element collection. */
    @ElementCollection
    @CollectionTable(name = "booking_passengers",
                     joinColumns = @JoinColumn(name = "booking_id"))
    @Column(name = "passenger_name")
    @Builder.Default
    private List<String> passengerNames = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
