package com.flightbookingapp.service.impl;

import com.flightbookingapp.dto.response.DashboardResponse;
import com.flightbookingapp.model.BookingStatus;
import com.flightbookingapp.model.PaymentStatus;
import com.flightbookingapp.repository.*;
import com.flightbookingapp.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * Aggregates system-wide statistics for the admin dashboard.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository    userRepository;
    private final FlightRepository  flightRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public DashboardResponse getDashboardStats() {
        LocalDateTime now       = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();

        BigDecimal totalRevenue   = bookingRepository.calculateRevenue(LocalDateTime.MIN, now);
        BigDecimal monthlyRevenue = bookingRepository.calculateRevenue(monthStart, now);

        return DashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalFlights(flightRepository.count())
                .totalBookings(bookingRepository.count())
                .pendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING))
                .confirmedBookings(bookingRepository.countByStatus(BookingStatus.CONFIRMED))
                .cancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED))
                .totalPayments(paymentRepository.countByPaymentStatus(PaymentStatus.COMPLETED))
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    @Override
    public BigDecimal getRevenueBetween(LocalDateTime from, LocalDateTime to) {
        return bookingRepository.calculateRevenue(from, to);
    }
}
