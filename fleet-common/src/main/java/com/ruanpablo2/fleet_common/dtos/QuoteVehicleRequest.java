package com.ruanpablo2.fleet_common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record QuoteVehicleRequest(
        @NotBlank(message = "Vehicle plate is required")
        String licensePlate,

        @NotBlank(message = "FIPE code is required")
        String fipeCode,

        @NotBlank(message = "Year ID is required")
        String yearId,

        @NotNull(message = "Coverage limit is required")
        BigDecimal coverageLimit
) {}