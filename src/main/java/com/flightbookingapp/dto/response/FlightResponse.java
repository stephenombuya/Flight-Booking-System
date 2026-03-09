package com.flightbookingapp.dto.response;

import com.flightbookingapp.model.FlightStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Public-facing flight information DTO. */
@Data
@Builder
public class FlightResponse {
    private Long          id;
    private String        flightNumber;
    private String        airline;
    private String        origin;
    private String        destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal    price;
    private Integer       totalSeats;
    private Integer       availableSeats;
    private FlightStatus  status;
    private long          durationMinutes;
}
