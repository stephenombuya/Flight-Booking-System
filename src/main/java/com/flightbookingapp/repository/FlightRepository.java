package com.flightbookingapp.repository;

import com.flightbookingapp.model.Flight;
import com.flightbookingapp.model.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Repository for flight search and availability queries.
 */
@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsByFlightNumber(String flightNumber);

    /**
     * Full-featured search used by the public flight-search endpoint.
     * All parameters are optional — null values are treated as "match all".
     */
    @Query("""
            SELECT f FROM Flight f
            WHERE (:origin      IS NULL OR LOWER(f.origin)      LIKE LOWER(CONCAT('%', :origin,      '%')))
              AND (:destination IS NULL OR LOWER(f.destination) LIKE LOWER(CONCAT('%', :destination, '%')))
              AND (:fromDate    IS NULL OR f.departureTime >= :fromDate)
              AND (:toDate      IS NULL OR f.departureTime <= :toDate)
              AND (:minPrice    IS NULL OR f.price >= :minPrice)
              AND (:maxPrice    IS NULL OR f.price <= :maxPrice)
              AND (:status      IS NULL OR f.status = :status)
              AND (:minSeats    IS NULL OR f.availableSeats >= :minSeats)
            """)
    Page<Flight> searchFlights(
            @Param("origin")      String origin,
            @Param("destination") String destination,
            @Param("fromDate")    LocalDateTime fromDate,
            @Param("toDate")      LocalDateTime toDate,
            @Param("minPrice")    BigDecimal minPrice,
            @Param("maxPrice")    BigDecimal maxPrice,
            @Param("status")      FlightStatus status,
            @Param("minSeats")    Integer minSeats,
            Pageable pageable
    );
}
