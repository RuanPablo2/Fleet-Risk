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

    @GetMapping("/{fipeCode}/years/{yearId}")
    public ResponseEntity<VehicleFipeResponse> getVehicle(@PathVariable String fipeCode, @PathVariable String yearId) {
        System.out.println("🔍 Received a request for FIPE: " + fipeCode + " | Year: " + yearId);

        VehicleFipeResponse response = vehicleService.getVehicleDetails(fipeCode, yearId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models/search")
    public ResponseEntity<List<VehicleModelSearchDTO>> searchModels(@RequestParam("query") String query) {
        List<VehicleModelSearchDTO> results = vehicleService.searchModelsLocally(query);
        return ResponseEntity.ok(results);
    }
}