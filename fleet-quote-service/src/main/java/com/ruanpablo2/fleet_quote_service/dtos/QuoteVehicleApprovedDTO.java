package com.ruanpablo2.fleet_quote_service.dtos;

import com.ruanpablo2.fleet_common.dtos.QuoteCoverageApprovedDTO;

import java.math.BigDecimal;
import java.util.List;

public record QuoteVehicleApprovedDTO(
        String modelName,
        String year,
        String licensePlate,
        BigDecimal fipeValue,
        BigDecimal calculatedPremium,
        List<QuoteCoverageApprovedDTO> coverages
) {}