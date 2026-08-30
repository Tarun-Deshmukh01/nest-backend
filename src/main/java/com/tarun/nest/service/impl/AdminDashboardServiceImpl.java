package com.tarun.nest.service.impl;

import com.tarun.nest.dto.AdminDashboardResponse;
import com.tarun.nest.repository.VendorRepository;
import com.tarun.nest.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final VendorRepository vendorRepository;

    @Override
    public AdminDashboardResponse getDashboard() {

        long totalVendors = vendorRepository.count();

        long pendingRequests =
                vendorRepository.countByStatus("PENDING");

        return new AdminDashboardResponse(
                totalVendors,
                pendingRequests
        );
    }
}