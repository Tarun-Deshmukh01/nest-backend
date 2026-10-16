package com.tarun.nest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull Long productId,
        @NotNull @Min(1) @Max(100) Integer quantity
) {
}
