package com.tarun.nest.service;

import com.tarun.nest.dto.AdminDashboardResponse;
import com.tarun.nest.dto.AdminDashboardStatsResponse;

public interface AdminDashboardService {

    AdminDashboardResponse getDashboard();

    AdminDashboardStatsResponse getDashboardStats();
}
