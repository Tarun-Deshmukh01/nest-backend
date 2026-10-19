package com.tarun.nest.dto;

public record CustomerAddressResponse(
        Long id,
        String label,
        String recipientName,
        String phoneNumber,
        String address,
        String addressLine2,
        String landmark,
        String city,
        String state,
        String pincode,
        String country,
        boolean defaultAddress
) {
}
