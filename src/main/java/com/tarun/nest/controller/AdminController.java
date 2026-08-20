package com.tarun.nest.controller;

import com.tarun.nest.dto.ApiResponse;
import com.tarun.nest.security.JwtAuthenticationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(Authentication authentication) {
        log.info("Fetching admin dashboard for: {}", authentication.getName());
        JwtAuthenticationDetails details = (JwtAuthenticationDetails) authentication.getDetails();
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Admin dashboard retrieved successfully",
                null
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        log.info("Admin {} fetching all users", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Users retrieved successfully",
                null
        ));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> getUserDetails(
            @PathVariable Long userId,
            Authentication authentication) {
        log.info("Admin {} fetching user {} details", authentication.getName(), userId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "User details retrieved successfully",
                null
        ));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Boolean active,
            Authentication authentication) {
        log.info("Admin {} updating user {} status to: {}", authentication.getName(), userId, active);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "User status updated successfully",
                null
        ));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {
        log.info("Admin {} deleting user {}", authentication.getName(), userId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "User deleted successfully",
                null
        ));
    }

    @GetMapping("/vendors")
    public ResponseEntity<ApiResponse> getAllVendors(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        log.info("Admin {} fetching all vendors", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendors retrieved successfully",
                null
        ));
    }

    @GetMapping("/vendors/{vendorId}")
    public ResponseEntity<ApiResponse> getVendorDetails(
            @PathVariable Long vendorId,
            Authentication authentication) {
        log.info("Admin {} fetching vendor {} details", authentication.getName(), vendorId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor details retrieved successfully",
                null
        ));
    }

    @PutMapping("/vendors/{vendorId}/approve")
    public ResponseEntity<ApiResponse> approveVendor(
            @PathVariable Long vendorId,
            Authentication authentication) {
        log.info("Admin {} approving vendor {}", authentication.getName(), vendorId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor approved successfully",
                null
        ));
    }

    @PutMapping("/vendors/{vendorId}/reject")
    public ResponseEntity<ApiResponse> rejectVendor(
            @PathVariable Long vendorId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Admin {} rejecting vendor {}", authentication.getName(), vendorId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor rejected successfully",
                null
        ));
    }

    @GetMapping("/vendor-requests")
    public ResponseEntity<ApiResponse> getVendorRequests(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        log.info("Admin {} fetching vendor requests", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor requests retrieved successfully",
                null
        ));
    }

    @DeleteMapping("/vendors/{vendorId}")
    public ResponseEntity<ApiResponse> deleteVendor(
            @PathVariable Long vendorId,
            Authentication authentication) {
        log.info("Admin {} deleting vendor {}", authentication.getName(), vendorId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Vendor deleted successfully",
                null
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        log.info("Admin {} fetching all orders", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Orders retrieved successfully",
                null
        ));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse> getOrderDetails(
            @PathVariable Long orderId,
            Authentication authentication) {
        log.info("Admin {} fetching order {} details", authentication.getName(), orderId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Order details retrieved successfully",
                null
        ));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Authentication authentication) {
        log.info("Admin {} fetching all products", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Products retrieved successfully",
                null
        ));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        log.info("Admin {} deleting product {}", authentication.getName(), productId);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Product deleted successfully",
                null
        ));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse> getAnalytics(
            @RequestParam(required = false) String period,
            Authentication authentication) {
        log.info("Admin {} fetching analytics", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Analytics retrieved successfully",
                null
        ));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse> getReports(
            @RequestParam(required = false) String type,
            Authentication authentication) {
        log.info("Admin {} fetching reports", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "Reports retrieved successfully",
                null
        ));
    }

    @PostMapping("/system/config")
    public ResponseEntity<ApiResponse> updateSystemConfig(
            @RequestBody String configData,
            Authentication authentication) {
        log.info("Admin {} updating system configuration", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "System configuration updated successfully",
                null
        ));
    }

    @GetMapping("/system/config")
    public ResponseEntity<ApiResponse> getSystemConfig(Authentication authentication) {
        log.info("Admin {} fetching system configuration", authentication.getName());
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "System configuration retrieved successfully",
                null
        ));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role,
            Authentication authentication) {
        log.info("Admin {} updating user {} role to: {}", authentication.getName(), userId, role);
        
        return ResponseEntity.ok(new ApiResponse(
                HttpStatus.OK.value(),
                "User role updated successfully",
                null
        ));
    }
}
