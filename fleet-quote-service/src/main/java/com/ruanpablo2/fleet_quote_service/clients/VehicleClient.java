package com.ruanpablo2.fleet_quote_service.clients;

import com.ruanpablo2.fleet_quote_service.dtos.VehicleFipeResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface VehicleClient {
    @GetExchange("/api/v1/vehicles/{fipeCode}/years/{yearId}")
    VehicleFipeResponseDTO getVehicleDetails(@PathVariable("fipeCode") String fipeCode,
                                             @PathVariable("yearId") String yearId);
}