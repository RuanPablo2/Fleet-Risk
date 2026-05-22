package com.ruanpablo2.fleet_quote_service.dtos;

public record QuoteKpiResponse(
        long pending,
        long calculated,
        long approved
) {}