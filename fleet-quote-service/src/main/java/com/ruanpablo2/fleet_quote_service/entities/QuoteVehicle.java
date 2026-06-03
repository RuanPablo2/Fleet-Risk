package com.ruanpablo2.fleet_quote_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quote_vehicles")
public class QuoteVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licensePlate;
    private String fipeCode;
    private String yearId;
    private String modelName;

    @Column(precision = 19, scale = 2)
    private BigDecimal fipeValue;

    @Column(precision = 19, scale = 2)
    private BigDecimal calculatedPremium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    @JsonIgnore
    private Quote quote;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleCoverage> coverages = new ArrayList<>();

    public QuoteVehicle() {
    }

    public QuoteVehicle(Long id, String licensePlate, String fipeCode, String yearId, String modelName, BigDecimal fipeValue, BigDecimal calculatedPremium, Quote quote) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.fipeCode = fipeCode;
        this.yearId = yearId;
        this.modelName = modelName;
        this.fipeValue = fipeValue;
        this.calculatedPremium = calculatedPremium;
        this.quote = quote;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getFipeCode() {
        return fipeCode;
    }

    public void setFipeCode(String fipeCode) {
        this.fipeCode = fipeCode;
    }

    public String getYearId() {
        return yearId;
    }

    public void setYearId(String yearId) {
        this.yearId = yearId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public BigDecimal getFipeValue() {
        return fipeValue;
    }

    public void setFipeValue(BigDecimal fipeValue) {
        this.fipeValue = fipeValue;
    }

    public BigDecimal getCalculatedPremium() {
        return calculatedPremium;
    }

    public void setCalculatedPremium(BigDecimal calculatedPremium) {
        this.calculatedPremium = calculatedPremium;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public List<VehicleCoverage> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<VehicleCoverage> coverages) {
        this.coverages = coverages;
    }

    public void addCoverage(VehicleCoverage coverage) {
        coverages.add(coverage);
        coverage.setVehicle(this);
    }
}