package com.tarun.nest.controller;

import com.tarun.nest.dto.AdminCustomersResponse;
import com.tarun.nest.dto.AdminDashboardResponse;
import com.tarun.nest.dto.AdminDashboardStatsResponse;
import com.tarun.nest.dto.ApiResponse;
import com.tarun.nest.dto.CustomerResponse;
import com.tarun.nest.dto.UpdateCustomerStatusRequest;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.service.AdminCustomerService;
import com.tarun.nest.service.AdminDashboardService;
import com.tarun.nest.service.VendorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final VendorService vendorService;
    private final AdminDashboardService adminDashboardService;
    private final AdminCustomerService adminCustomerService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(
            Authentication authentication) {

        log.info(
                "Fetching admin dashboard for: {}",
                authentication.getName()
        );

        AdminDashboardResponse dashboard =
                adminDashboardService.getDashboard();

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Admin dashboard retrieved successfully",
                        dashboard
                )
        );
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse> getDashboardStats(
            Authentication authentication) {

        log.info(
                "Fetching admin dashboard stats for: {}",
                authentication.getName()
        );

        AdminDashboardStatsResponse stats =
                adminDashboardService.getDashboardStats();

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Admin dashboard stats retrieved successfully",
                        stats
                )
        );
    }


    @GetMapping("/vendor-requests")
    public ResponseEntity<ApiResponse> getVendorRequests(
            Authentication authentication) {

        log.info(
                "Admin {} fetching vendor requests",
                authentication.getName()
        );

        List<VendorResponse> vendors =
                vendorService.getPendingVendors();

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Pending vendor requests retrieved successfully",
                        vendors
                )
        );
    }
    @GetMapping("/vendors/pending")
    public ResponseEntity<ApiResponse> getPendingVendors(
            Authentication authentication) {

        log.info(
                "Fetching pending vendors for admin: {}",
                authentication.getName()
        );

        List<VendorResponse> vendors =
                vendorService.getPendingVendors();

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Pending vendors retrieved successfully",
                        vendors
                )
        );
    }
    @PatchMapping("/vendors/{vendorId}/approve")
    public ResponseEntity<ApiResponse> approveVendor(
            @PathVariable Long vendorId,
            Authentication authentication) {

        log.info(
                "Admin {} approving vendor {}",
                authentication.getName(),
                vendorId
        );

        vendorService.approveVendor(vendorId);

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Vendor approved successfully",
                        null
                )
        );
    }
    @PatchMapping("/vendors/{vendorId}/decline")
    public ResponseEntity<ApiResponse> declineVendor(
            @PathVariable Long vendorId,
            Authentication authentication) {

        log.info(
                "Admin {} declining vendor {}",
                authentication.getName(),
                vendorId
        );

        vendorService.declineVendor(vendorId);

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Vendor declined successfully",
                        null
                )
        );
    }
    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse> getAllVendors(
            Authentication authentication) {

        log.info(
                "Fetching all vendors for admin: {}",
                authentication.getName()
        );

        List<VendorResponse> vendors =
                vendorService.getAllVendors();

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "All vendors retrieved successfully",
                        vendors
                )
        );
    }

    @GetMapping("/customers")
    public ResponseEntity<AdminCustomersResponse> getAllCustomers(
            Authentication authentication) {

        log.info(
                "Admin {} fetching all customers",
                authentication.getName()
        );

        return ResponseEntity.ok(
                adminCustomerService.getAllCustomers()
        );
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long customerId,
            Authentication authentication) {

        log.info(
                "Admin {} fetching customer {}",
                authentication.getName(),
                customerId
        );

        return ResponseEntity.ok(
                adminCustomerService.getCustomerById(customerId)
        );
    }

    @PatchMapping("/customers/{customerId}/status")
    public ResponseEntity<ApiResponse> updateCustomerStatus(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerStatusRequest request,
            Authentication authentication) {

        log.info(
                "Admin {} updating status of customer {} to active={}",
                authentication.getName(),
                customerId,
                request.getActive()
        );

        adminCustomerService.updateCustomerStatus(
                customerId,
                request.getActive()
        );

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Customer status updated successfully",
                        null
                )
        );
    }
}
