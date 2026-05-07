package com.ruanpablo2.fleet_vehicle_service.services;

import com.ruanpablo2.fleet_vehicle_service.clients.FipeClient;
import com.ruanpablo2.fleet_vehicle_service.config.RabbitMQConfig;
import com.ruanpablo2.fleet_vehicle_service.dtos.BrandDTO;
import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleModelSearchDTO;
import com.ruanpablo2.fleet_vehicle_service.models.Brand;
import com.ruanpablo2.fleet_vehicle_service.models.VehicleModel;
import com.ruanpablo2.fleet_vehicle_service.repositories.BrandRepository;
import com.ruanpablo2.fleet_vehicle_service.repositories.VehicleModelRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    private final FipeClient fipeClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final VehicleModelRepository vehicleModelRepository;

    public VehicleService(FipeClient fipeClient,
                          RedisTemplate<String, Object> redisTemplate,
                          ObjectMapper objectMapper,
                          RabbitTemplate rabbitTemplate,
                          VehicleModelRepository vehicleModelRepository) {
        this.fipeClient = fipeClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.vehicleModelRepository = vehicleModelRepository;
    }

    public VehicleFipeResponse getVehicleDetails(String fipeCode, String yearId) {
        String key = "fipe:" + fipeCode + ":" + yearId;
        Object cachedData = redisTemplate.opsForValue().get(key);

        if (cachedData != null) {
            System.out.println("⚡ [CACHE HIT] Returning from Redis...");
            sendToRabbit(cachedData);
            return objectMapper.convertValue(cachedData, VehicleFipeResponse.class);
        }

        System.out.println("☁️ [CACHE MISS] Searching the Parallelum API...");
        VehicleFipeResponse response = fipeClient.fetchVehicleData(fipeCode, yearId);

        if (response != null) {
            redisTemplate.opsForValue().set(key, response, Duration.ofHours(24));
            sendToRabbit(response);
        }

        return response;
    }

    private void sendToRabbit(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_VEHICLE,
                    RabbitMQConfig.ROUTING_KEY_CONSULTED,
                    json
            );
            System.out.println("✉️ [RABBITMQ] Event sent to the Exchange!");
        } catch (Exception e) {
            System.err.println("❌ Error sending to RabbitMQ: " + e.getMessage());
        }
    }

    public List<VehicleModelSearchDTO> searchModelsLocally(String query) {
        System.out.println("🔍 Searching models locally for query: " + query);

        List<VehicleModel> models = vehicleModelRepository.findTop20ByNameContainingIgnoreCase(query);

        return models.stream()
                .map(model -> new VehicleModelSearchDTO(
                        model.getId(),
                        model.getName(),
                        model.getBrand().getName()
                ))
                .collect(Collectors.toList());
    }
}