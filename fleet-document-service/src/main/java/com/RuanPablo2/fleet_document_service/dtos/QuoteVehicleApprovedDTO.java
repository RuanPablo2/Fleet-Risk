package com.RuanPablo2.fleet_document_service.dtos;

import java.math.BigDecimal;

public record QuoteVehicleApprovedDTO(
        String modelName,
        String year,
        String licensePlate,
        BigDecimal fipeValue,
        BigDecimal calculatedPremium
) {}