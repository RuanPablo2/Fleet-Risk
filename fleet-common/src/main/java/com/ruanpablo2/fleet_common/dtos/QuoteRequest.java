package com.ruanpablo2.fleet_common.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuoteRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotEmpty(message = "A fleet must have at least one vehicle")
        @Valid
        List<QuoteVehicleRequest> vehicles
) {}