package com.ruanpablo2.fleet_quote_service.dtos;

import java.math.BigDecimal;

public record QuoteVehicleApprovedDTO(
        String modelName,
        String year,
        String licensePlate,
        BigDecimal fipeValue,
        BigDecimal calculatedPremium
) {}