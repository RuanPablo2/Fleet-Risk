package com.ruanpablo2.fleet_common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record QuoteRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Vehicle plate is required")
        String licensePlate,

        @NotNull(message = "FIPE value is required")
        BigDecimal fipeValue
) {}