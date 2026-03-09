package com.flightbookingapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the full Spring context loads without errors.
 */
@SpringBootTest
@ActiveProfiles("test")
class FlightBookingApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, the entire application context wired up correctly
    }
}
