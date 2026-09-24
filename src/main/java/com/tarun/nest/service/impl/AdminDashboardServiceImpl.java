package com.tarun.nest.service.impl;

import com.tarun.nest.dto.AdminDashboardResponse;
import com.tarun.nest.dto.AdminDashboardStatsResponse;
import com.tarun.nest.entity.Role;
import com.tarun.nest.repository.ProductRepository;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.repository.VendorRepository;
import com.tarun.nest.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public AdminDashboardResponse getDashboard() {

        long totalVendors = vendorRepository.count();

        long pendingRequests =
                vendorRepository.countByStatus("PENDING");

        long approvedVendors =
                vendorRepository.countByStatus("APPROVED");

        long declinedVendors =
                vendorRepository.countByStatus("REJECTED");

        return new AdminDashboardResponse(
                totalVendors,
                pendingRequests,
                approvedVendors,
                declinedVendors
        );
    }

    @Override
    public AdminDashboardStatsResponse getDashboardStats() {

        long totalProducts = productRepository.count();
        long totalVendors = vendorRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);

        return new AdminDashboardStatsResponse(
                totalProducts,
                totalVendors,
                totalCustomers
        );
    }
}
