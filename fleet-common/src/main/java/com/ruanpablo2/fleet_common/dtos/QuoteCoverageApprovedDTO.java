package com.ruanpablo2.fleet_common.dtos;

import java.math.BigDecimal;

public record QuoteCoverageApprovedDTO(
        String type,
        BigDecimal fipePercentage,
        BigDecimal limitAmount
) {}