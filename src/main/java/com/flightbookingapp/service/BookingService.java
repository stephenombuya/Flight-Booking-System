package com.flightbookingapp.service;

import com.flightbookingapp.dto.request.BookingRequest;
import com.flightbookingapp.dto.response.BookingResponse;
import com.flightbookingapp.dto.response.PagedResponse;
import com.flightbookingapp.model.User;
import org.springframework.data.domain.Pageable;

/** Contract for booking lifecycle management. */
public interface BookingService {
    BookingResponse createBooking(BookingRequest request, User currentUser);
    BookingResponse getBookingById(Long id, User currentUser);
    BookingResponse getBookingByReference(String reference, User currentUser);
    PagedResponse<BookingResponse> getUserBookings(Long userId, User currentUser, Pageable pageable);
    PagedResponse<BookingResponse> getAllBookings(Pageable pageable);
    BookingResponse confirmBooking(Long id);
    BookingResponse cancelBooking(Long id, User currentUser);
}
