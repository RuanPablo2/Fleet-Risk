package com.ruanpablo2.fleet_quote_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruanpablo2.fleet_common.enums.CoverageType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_coverages")
public class VehicleCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoverageType type;

    @Column(precision = 5, scale = 2)
    private BigDecimal fipePercentage;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal limitAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_vehicle_id", nullable = false)
    @JsonIgnore
    private QuoteVehicle vehicle;

    public VehicleCoverage() {
    }

    public VehicleCoverage(CoverageType type, BigDecimal fipePercentage, BigDecimal limitAmount, BigDecimal premiumAmount, QuoteVehicle vehicle) {
        this.type = type;
        this.fipePercentage = fipePercentage;
        this.limitAmount = limitAmount;
        this.premiumAmount = premiumAmount;
        this.vehicle = vehicle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CoverageType getType() {
        return type;
    }

    public void setType(CoverageType type) {
        this.type = type;
    }

    public BigDecimal getFipePercentage() {
        return fipePercentage;
    }

    public void setFipePercentage(BigDecimal fipePercentage) {
        this.fipePercentage = fipePercentage;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public QuoteVehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(QuoteVehicle vehicle) {
        this.vehicle = vehicle;
    }
}