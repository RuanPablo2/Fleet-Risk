package com.ruanpablo2.fleet_common.dtos;

import java.math.BigDecimal;

public record QuoteVehicleEventDTO(
        Long vehicleId,
        String fipeCode,
        String yearId,
        BigDecimal coverageLimit
) {}