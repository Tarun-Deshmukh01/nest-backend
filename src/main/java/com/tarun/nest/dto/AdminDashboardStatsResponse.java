package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardStatsResponse {

    private long totalProducts;
    private long totalVendors;
    private long totalCustomers;
}
