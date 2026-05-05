package com.ruanpablo2.fleet_vehicle_service.services;

import com.ruanpablo2.fleet_vehicle_service.clients.FipeClient;
import com.ruanpablo2.fleet_vehicle_service.config.RabbitMQConfig;
import com.ruanpablo2.fleet_vehicle_service.dtos.BrandDTO;
import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import com.ruanpablo2.fleet_vehicle_service.models.Brand;
import com.ruanpablo2.fleet_vehicle_service.repositories.BrandRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Service
public class VehicleService {

    private final FipeClient fipeClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final BrandRepository brandRepository;

    public VehicleService(FipeClient fipeClient,
                          RedisTemplate<String, Object> redisTemplate,
                          ObjectMapper objectMapper,
                          RabbitTemplate rabbitTemplate,
                          BrandRepository brandRepository) {
        this.fipeClient = fipeClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.brandRepository = brandRepository;
    }

    public void startFullSync() {
        System.out.println("🚀 Searching for brands on API Parallelum...");
        List<BrandDTO> brands = fipeClient.getAllBrands();

        for (BrandDTO dto : brands) {
            Brand brand = brandRepository.findByCode(dto.code())
                    .orElseGet(() -> brandRepository.save(new Brand(dto.name(), dto.code())));

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_VEHICLE,
                    RabbitMQConfig.ROUTING_KEY_SYNC_MODELS,
                    brand.getId()
            );
        }
        System.out.println("📬 All the brands have been sent to the processing queue!");
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
}