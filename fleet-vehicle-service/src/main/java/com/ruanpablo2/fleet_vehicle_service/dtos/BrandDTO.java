package com.ruanpablo2.fleet_vehicle_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BrandDTO(
        @JsonProperty("name") String name,
        @JsonProperty("code") String code
) {}