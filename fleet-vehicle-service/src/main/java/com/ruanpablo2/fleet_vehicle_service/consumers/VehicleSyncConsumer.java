package com.ruanpablo2.fleet_vehicle_service.consumers;

import com.ruanpablo2.fleet_vehicle_service.clients.FipeClient;
import com.ruanpablo2.fleet_vehicle_service.config.RabbitMQConfig;
import com.ruanpablo2.fleet_vehicle_service.dtos.ModelDTO;
import com.ruanpablo2.fleet_vehicle_service.models.Brand;
import com.ruanpablo2.fleet_vehicle_service.models.VehicleModel;
import com.ruanpablo2.fleet_vehicle_service.repositories.BrandRepository;
import com.ruanpablo2.fleet_vehicle_service.repositories.VehicleModelRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleSyncConsumer {

    private final FipeClient fipeClient;
    private final VehicleModelRepository modelRepository;
    private final BrandRepository brandRepository;

    public VehicleSyncConsumer(FipeClient fipeClient,
                               VehicleModelRepository modelRepository,
                               BrandRepository brandRepository) {
        this.fipeClient = fipeClient;
        this.modelRepository = modelRepository;
        this.brandRepository = brandRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYNC_MODELS)
    public void processModelSync(Long brandId) {
        Brand brand = brandRepository.findById(brandId).orElseThrow();

        System.out.println("⏳ Downloading models for: " + brand.getName());

        List<ModelDTO> modelsResponse = fipeClient.getModelsByBrand(brand.getCode());

        List<VehicleModel> models = modelsResponse.stream()
                .map(m -> new VehicleModel(m.name(), brand))
                .toList();

        modelRepository.saveAll(models);
        System.out.println("✅ " + models.size() + " models saved for " + brand.getName());
    }
}