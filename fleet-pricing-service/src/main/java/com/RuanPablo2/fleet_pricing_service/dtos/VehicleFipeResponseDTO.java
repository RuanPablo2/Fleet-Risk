package com.RuanPablo2.fleet_pricing_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VehicleFipeResponseDTO(
        String model,
        String brand,
        @JsonProperty("codeFipe")
        String fipeCode,
        String yearId,
        String price,
        String fuel
) {}