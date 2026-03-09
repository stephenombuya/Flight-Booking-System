package com.flightbookingapp.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/** Payload for creating a new booking. */
@Data
public class BookingRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "At least one passenger is required")
    @Max(value = 9, message = "Cannot book more than 9 seats at once")
    private Integer numberOfPassengers;

    @NotEmpty(message = "Passenger names are required")
    @Size(min = 1, max = 9, message = "Passenger names list size must match number of passengers")
    private List<@NotBlank(message = "Passenger name must not be blank") String> passengerNames;
}
