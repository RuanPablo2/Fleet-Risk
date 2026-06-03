package com.ruanpablo2.fleet_common.dtos;

import com.ruanpablo2.fleet_common.enums.CoverageType;

import java.math.BigDecimal;

public record VehicleCoverageRequest(
        CoverageType type,
        BigDecimal fipePercentage,
        BigDecimal limitAmount
) {}