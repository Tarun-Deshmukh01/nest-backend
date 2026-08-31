package com.tarun.nest.dto;

public record LoginResponse(
        String token,
        Long userId,
        String email,
        String name,
        String role,
        String vendorStatus,
        boolean vendorActive
) {
}