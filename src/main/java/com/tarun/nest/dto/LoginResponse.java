package com.tarun.nest.dto;

public record LoginResponse(
        String token,
        String email
) {
}