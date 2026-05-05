package com.ruanpablo2.fleet_vehicle_service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ModelWrapperDTO(
        List<ModelDTO> models
) {}