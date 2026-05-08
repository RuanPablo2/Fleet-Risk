package com.ruanpablo2.fleet_quote_service.dtos;

import java.math.BigDecimal;

public record QuoteResponse(
        Long id,
        String customerName,
        BigDecimal totalPremium,
        String status
) {}