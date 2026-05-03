package com.ruanpablo2.fleet_vehicle_service.controllers;

import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import com.ruanpablo2.fleet_vehicle_service.services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{fipeCode}/{yearId}")
    public ResponseEntity<VehicleFipeResponse> getVehicle(@PathVariable String fipeCode, @PathVariable String yearId) {
        VehicleFipeResponse response = vehicleService.getVehicleDetails(fipeCode, yearId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}