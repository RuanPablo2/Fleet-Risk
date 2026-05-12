package com.RuanPablo2.fleet_document_service.dtos;

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