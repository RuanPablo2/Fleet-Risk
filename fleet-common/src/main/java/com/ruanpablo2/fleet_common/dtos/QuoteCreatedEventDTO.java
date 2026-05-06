package com.ruanpablo2.fleet_common.dtos;

import java.math.BigDecimal;
import java.util.List;

public record QuoteCreatedEventDTO(
        Long quoteId,
        List<QuoteVehicleEventDTO> vehicles
) {}