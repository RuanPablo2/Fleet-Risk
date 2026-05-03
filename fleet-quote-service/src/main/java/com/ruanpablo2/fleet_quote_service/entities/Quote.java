package com.ruanpablo2.fleet_quote_service.entities;

import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private String licensePlate;

    private String vehicleModel;

    private String modelYear;

    private BigDecimal fipeValue;

    private BigDecimal totalPremium;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = QuoteStatus.PENDING;
    }

    public Quote() {
    }

    public Quote(Long id, String customerName, String licensePlate, String vehicleModel, String modelYear, BigDecimal fipeValue, BigDecimal totalPremium, QuoteStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.licensePlate = licensePlate;
        this.vehicleModel = vehicleModel;
        this.modelYear = modelYear;
        this.fipeValue = fipeValue;
        this.totalPremium = totalPremium;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getModelYear() {
        return modelYear;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }

    public BigDecimal getFipeValue() {
        return fipeValue;
    }

    public void setFipeValue(BigDecimal fipeValue) {
        this.fipeValue = fipeValue;
    }

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public void setStatus(QuoteStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}