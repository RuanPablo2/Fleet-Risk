package com.ruanpablo2.fleet_vehicle_service.services;

import com.ruanpablo2.fleet_vehicle_service.clients.FipeClient;
import com.ruanpablo2.fleet_vehicle_service.dtos.VehicleFipeResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class VehicleService {

    private final FipeClient fipeClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public VehicleService(FipeClient fipeClient, RedisTemplate<String, Object> redisTemplate) {
        this.fipeClient = fipeClient;
        this.redisTemplate = redisTemplate;
    }

    public VehicleFipeResponse getVehicleDetails(String fipeCode, String yearId) {
        String cacheKey = "fipe:" + fipeCode + ":" + yearId;

        VehicleFipeResponse cached = (VehicleFipeResponse) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            System.out.println("⚡ [CACHE HIT] Returning from Redis: " + cacheKey);
            return cached;
        }

        System.out.println("☁️ [CACHE MISS] Searching the Parallelum API...");
        VehicleFipeResponse response = fipeClient.fetchVehicleData(fipeCode, yearId);

        if (response != null) {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(24));
        }

        return response;
    }
}