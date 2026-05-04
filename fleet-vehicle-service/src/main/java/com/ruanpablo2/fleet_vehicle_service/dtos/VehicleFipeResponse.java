package com.ruanpablo2.fleet_vehicle_service.dtos;

import java.io.Serializable;

public record VehicleFipeResponse(
        String price,
        String brand,
        String model,
        Integer modelYear,
        String fuel,
        String codeFipe,
        String referenceMonth,
        Integer vehicleType,
        String fuelAcronym
) implements Serializable {
    private static final long serialVersionUID = 1L;
}