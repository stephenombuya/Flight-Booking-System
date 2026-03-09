package com.flightbookingapp.dto.request;

import com.flightbookingapp.model.FlightStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Query parameters used by the public flight-search endpoint. */
@Data
public class FlightSearchRequest {

    private String origin;
    private String destination;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private FlightStatus status;

    /** Minimum seats that must still be available. */
    private Integer minSeats;
}
