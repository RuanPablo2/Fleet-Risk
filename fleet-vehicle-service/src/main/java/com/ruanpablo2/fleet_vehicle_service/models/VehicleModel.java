package com.ruanpablo2.fleet_vehicle_service.models;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_models")
public class VehicleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "fipe_code", unique = true)
    private String fipeCode;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    public VehicleModel() {}

    public VehicleModel(String name, Brand brand, String fipeCode) {
        this.name = name;
        this.brand = brand;
        this.fipeCode = fipeCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFipeCode() {
        return fipeCode;
    }

    public void setFipeCode(String fipeCode) {
        this.fipeCode = fipeCode;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}