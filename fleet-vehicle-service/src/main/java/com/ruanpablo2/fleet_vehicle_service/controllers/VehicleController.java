package com.ruanpablo2.fleet_vehicle_service.controllers;

import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleModelSearchDTO;
import com.ruanpablo2.fleet_vehicle_service.services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{fipeCode}/{yearId}")
    public ResponseEntity<VehicleFipeResponse> getVehicle(@PathVariable String fipeCode, @PathVariable String yearId) {
        System.out.println("Received a request for: " + fipeCode + " year: " + yearId);
        VehicleFipeResponse response = vehicleService.getVehicleDetails(fipeCode, yearId);

        if (response == null) {
            System.out.println("❌ Vehicle not found in FIPE or service error.");
        }

        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    @GetMapping("/models/search")
    public ResponseEntity<List<VehicleModelSearchDTO>> searchModels(@RequestParam("query") String query) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.badRequest().build();
        }

        List<VehicleModelSearchDTO> results = vehicleService.searchModelsLocally(query);
        return ResponseEntity.ok(results);
    }
}