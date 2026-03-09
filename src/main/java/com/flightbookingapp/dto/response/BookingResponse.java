package com.flightbookingapp.dto.response;

import com.flightbookingapp.model.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Booking detail response returned to the client. */
@Data
@Builder
public class BookingResponse {
    private Long          id;
    private String        bookingReference;
    private UserResponse  user;
    private FlightResponse flight;
    private Integer       numberOfPassengers;
    private BigDecimal    totalAmount;
    private BookingStatus status;
    private List<String>  passengerNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
