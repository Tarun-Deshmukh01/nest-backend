package com.tarun.nest.dto;

public record CustomerAddressResponse(
        Long id,
        String label,
        String address,
        String city,
        String state,
        String pincode,
        String country
) {
}
