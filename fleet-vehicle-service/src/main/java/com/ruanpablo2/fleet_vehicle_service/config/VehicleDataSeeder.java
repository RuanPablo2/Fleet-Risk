package com.ruanpablo2.fleet_vehicle_service.config;

import com.ruanpablo2.fleet_vehicle_service.models.Brand;
import com.ruanpablo2.fleet_vehicle_service.models.VehicleModel;
import com.ruanpablo2.fleet_vehicle_service.repositories.BrandRepository;
import com.ruanpablo2.fleet_vehicle_service.repositories.VehicleModelRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class VehicleDataSeeder implements CommandLineRunner {

    private final VehicleModelRepository modelRepository;
    private final BrandRepository brandRepository;
    private static final int BATCH_SIZE = 1000;

    public VehicleDataSeeder(VehicleModelRepository modelRepository, BrandRepository brandRepository) {
        this.modelRepository = modelRepository;
        this.brandRepository = brandRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (modelRepository.count() > 0) {
            System.out.println("👍 [SEED] Database already populated. Skipping CSV import.");
            return;
        }

        System.out.println("🌱 [SEED] Initiating a massive upload of the FIPE catalog...");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("vehicles.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            List<VehicleModel> batchList = new ArrayList<>();
            Map<String, Brand> brandCache = new HashMap<>();

            Set<String> processedFipes = new HashSet<>();

            int count = 0;

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length < 4) continue;

                String fipeCode = data[1].trim();
                String brandName = data[2].trim();
                String modelName = data[3].trim();

                if (processedFipes.contains(fipeCode)) {
                    continue;
                }

                Brand brand = brandCache.computeIfAbsent(brandName, name ->
                        brandRepository.findByName(name)
                                .orElseGet(() -> brandRepository.save(new Brand(name)))
                );

                VehicleModel model = new VehicleModel();
                model.setName(modelName);
                model.setFipeCode(fipeCode);
                model.setBrand(brand);

                batchList.add(model);
                processedFipes.add(fipeCode);
                count++;

                if (batchList.size() >= BATCH_SIZE) {
                    modelRepository.saveAll(batchList);
                    batchList.clear();
                    System.out.println("   -> Processed " + count + " unique models...");
                }
            }

            if (!batchList.isEmpty()) {
                modelRepository.saveAll(batchList);
            }

            System.out.println("✅ [SEED] Import completed! Total: " + count + " unique models embedded in Postgres.");

        } catch (Exception e) {
            System.err.println("❌ [SEED] Critical error during import: " + e.getMessage());
            e.printStackTrace();
        }
    }
}