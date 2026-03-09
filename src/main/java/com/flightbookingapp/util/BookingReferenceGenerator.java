package com.flightbookingapp.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates human-readable, sortable booking reference codes.
 * Format: FBS-YYYYMMDD-{zero-padded-sequence}
 * Example: FBS-20240315-00042
 *
 * <p>The sequence counter is in-memory and resets on application restart;
 * for production use a DB sequence or UUID-based strategy.</p>
 */
@Component
public class BookingReferenceGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong counter = new AtomicLong(0);

    public String generate() {
        return String.format("FBS-%s-%05d",
                LocalDate.now().format(DATE_FMT),
                counter.incrementAndGet());
    }
}
