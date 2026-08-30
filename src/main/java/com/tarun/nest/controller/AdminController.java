package com.tarun.nest.controller;

import com.tarun.nest.dto.ApiResponse;
import com.tarun.nest.dto.VendorResponse;
import com.tarun.nest.service.VendorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final VendorService vendorService;

    // =========================
    // Admin Dashboard
    // =========================
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(
            Authentication authentication) {

        log.info(
                "Fetching admin dashboard for: {}",
                authentication.getName()
        );

        return ResponseEntity.ok(
                new ApiResponse(
                        HttpStatus.OK.value(),
                        "Admin dashboard retrieved successfully",
                        null
                )
        );
    }

    // =========================
    // Pending Vendor Requests
    // =========================
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
}