package com.flightbookingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Flight Booking System application.
 * Bootstraps the Spring Boot context and enables scheduled tasks.
 */
@SpringBootApplication
@EnableScheduling
public class FlightBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightBookingApplication.class, args);
    }
}
