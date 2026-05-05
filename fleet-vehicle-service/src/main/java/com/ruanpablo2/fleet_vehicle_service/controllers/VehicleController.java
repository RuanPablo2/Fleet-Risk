package com.ruanpablo2.fleet_vehicle_service.controllers;

import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import com.ruanpablo2.fleet_vehicle_service.services.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
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

    @PostMapping("/sync")
    public ResponseEntity<String> syncFipeData() {
        try {
            vehicleService.startFullSync();
            return ResponseEntity.ok("🚀 Synchronization has started! Follow the progress in the console logs.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ Error starting synchronization: " + e.getMessage());
        }
    }
}