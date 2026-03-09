package com.flightbookingapp.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Aggregated statistics shown on the admin dashboard. */
@Data
@Builder
public class DashboardResponse {
    private long       totalUsers;
    private long       totalFlights;
    private long       totalBookings;
    private long       pendingBookings;
    private long       confirmedBookings;
    private long       cancelledBookings;
    private long       totalPayments;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
}
