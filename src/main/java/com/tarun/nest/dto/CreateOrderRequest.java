package com.tarun.nest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String pincode;

    @NotBlank
    private String country;
}