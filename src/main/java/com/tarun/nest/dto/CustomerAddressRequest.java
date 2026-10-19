package com.tarun.nest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerAddressRequest(
        @Size(max = 50) String label,
        @Size(max = 120) String recipientName,
        @Size(max = 30) String phoneNumber,
        @JsonAlias("addressLine1")
        @NotBlank @Size(max = 255) String address,
        @Size(max = 255) String addressLine2,
        @Size(max = 255) String landmark,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String state,
        @JsonAlias("postalCode")
        @NotBlank @Size(max = 20) String pincode,
        @NotBlank @Size(max = 100) String country,
        Boolean defaultAddress
) {
}
