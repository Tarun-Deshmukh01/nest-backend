package com.tarun.nest.controller;

import com.tarun.nest.dto.AdminDashboardResponse;
import com.tarun.nest.dto.ApiResponse;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.service.AdminDashboardService;
import com.tarun.nest.service.VendorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}