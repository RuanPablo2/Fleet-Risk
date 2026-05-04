package com.ruanpablo2.fleet_vehicle_service.clients;

import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FipeClient {

    private final RestClient restClient;

    public FipeClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://parallelum.com.br/fipe/api/v2").build();
    }

    public VehicleFipeResponse fetchVehicleData(String fipeCode, String yearId) {
        try {
            return restClient.get()
                    .uri("/cars/{fipeCode}/years/{yearId}", fipeCode, yearId)
                    .retrieve()
                    .body(VehicleFipeResponse.class);
        } catch (Exception e) {
            System.err.println("Error when consulting FIPE: " + e.getMessage());
            return null;
        }
    }
}