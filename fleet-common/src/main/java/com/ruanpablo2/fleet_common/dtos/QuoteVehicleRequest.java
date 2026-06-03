package com.ruanpablo2.fleet_common.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuoteVehicleRequest(
        @NotBlank(message = "Vehicle plate is required")
        String licensePlate,

        @NotBlank(message = "FIPE code is required")
        String fipeCode,

        @NotBlank(message = "Year ID is required")
        String yearId,

        @NotEmpty(message = "The vehicle must have at least one cover")
        List<VehicleCoverageRequest> coverages
) {}