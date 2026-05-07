package com.ruanpablo2.fleet_common.dtos;

import java.math.BigDecimal;

public record QuoteVehicleCalculatedEventDTO(
        Long vehicleId,
        BigDecimal calculatedPremium
) {}