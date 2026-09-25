package com.tarun.nest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;
}