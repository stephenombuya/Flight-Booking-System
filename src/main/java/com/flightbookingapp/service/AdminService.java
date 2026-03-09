package com.flightbookingapp.service;

import com.flightbookingapp.dto.response.DashboardResponse;

import java.time.LocalDateTime;

/** Contract for admin reporting and dashboard aggregation. */
public interface AdminService {
    DashboardResponse getDashboardStats();
    java.math.BigDecimal getRevenueBetween(LocalDateTime from, LocalDateTime to);
}
