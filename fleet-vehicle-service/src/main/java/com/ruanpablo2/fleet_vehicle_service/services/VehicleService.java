package com.ruanpablo2.fleet_vehicle_service.services;

import com.ruanpablo2.fleet_vehicle_service.clients.FipeClient;
import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
public class VehicleService {

    private final FipeClient fipeClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public VehicleService(FipeClient fipeClient, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.fipeClient = fipeClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public VehicleFipeResponse getVehicleDetails(String fipeCode, String yearId) {
        String key = "fipe:" + fipeCode + ":" + yearId;
        Object cachedData = redisTemplate.opsForValue().get(key);

        if (cachedData != null) {
            System.out.println("⚡ [CACHE HIT] Returning from Redis...");
            return objectMapper.convertValue(cachedData, VehicleFipeResponse.class);
        }

        System.out.println("☁️ [CACHE MISS] Searching the Parallelum API...");
        VehicleFipeResponse response = fipeClient.fetchVehicleData(fipeCode, yearId);

        if (response != null) {
            redisTemplate.opsForValue().set(key, response, Duration.ofHours(24));
        }

        return response;
    }
}