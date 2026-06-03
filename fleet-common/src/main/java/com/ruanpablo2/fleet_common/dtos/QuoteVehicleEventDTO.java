package com.ruanpablo2.fleet_common.dtos;

import java.util.List;

public record QuoteVehicleEventDTO(
        Long id,
        String fipeCode,
        String yearId,
        List<VehicleCoverageEventDTO> coverages
) {}