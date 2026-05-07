package com.ruanpablo2.fleet_vehicle_service.repositories;

import com.ruanpablo2.fleet_vehicle_service.models.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    List<VehicleModel> findTop20ByNameContainingIgnoreCase(String name);

    List<VehicleModel> findTop20ByNameContainingIgnoreCaseOrFipeCodeContainingIgnoreCase(String name, String fipeCode);
}