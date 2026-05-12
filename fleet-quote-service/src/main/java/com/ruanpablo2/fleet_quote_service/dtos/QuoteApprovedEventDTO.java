package com.ruanpablo2.fleet_quote_service.dtos;

import java.math.BigDecimal;
import java.util.List;

public record QuoteApprovedEventDTO(
        Long quoteId,
        String customerName,
        String customerCnpj,
        String brokerName,
        BigDecimal totalPremium,
        BigDecimal totalFipe,
        List<QuoteVehicleApprovedDTO> vehicles
) {}