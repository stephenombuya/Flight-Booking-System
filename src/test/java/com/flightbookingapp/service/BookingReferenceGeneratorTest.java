package com.flightbookingapp.service;

import com.flightbookingapp.repository.BookingRepository;
import com.flightbookingapp.util.BookingReferenceGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingReferenceGenerator Unit Tests")
class BookingReferenceGeneratorTest {

    @Mock private BookingRepository bookingRepository;
    @InjectMocks private BookingReferenceGenerator generator;

    @RepeatedTest(10)
    @DisplayName("generate() always returns an 8-character uppercase alphanumeric reference")
    void generate_formatIsCorrect() {
        when(bookingRepository.existsByBookingReference(anyString())).thenReturn(false);

        String ref = generator.generate();

        assertThat(ref).matches("[A-Z]{2}[0-9]{6}");
    }

    @Test
    @DisplayName("generate() retries when a collision occurs")
    void generate_retriesOnCollision() {
        // First call collides, second does not
        when(bookingRepository.existsByBookingReference(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        String ref = generator.generate();

        assertThat(ref).isNotBlank();
        verify(bookingRepository, times(2)).existsByBookingReference(anyString());
    }
}
