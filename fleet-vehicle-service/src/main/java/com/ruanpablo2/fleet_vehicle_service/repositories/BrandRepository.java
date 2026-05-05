package com.ruanpablo2.fleet_vehicle_service.repositories;

import com.ruanpablo2.fleet_vehicle_service.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByCode(String code);
}