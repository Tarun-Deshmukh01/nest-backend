package com.tarun.nest.dto;

public record LoginResponse(
        String token,
        Long id,
        String email,
        String role
) {
}