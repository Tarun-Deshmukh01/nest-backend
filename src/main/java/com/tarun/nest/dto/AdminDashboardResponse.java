package com.tarun.nest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalVendors;
    private long pendingRequests;
    private long approvedVendors;
    private long declinedVendors;
}