package com.ruanpablo2.fleet_quote_service.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_vehicles")
public class QuoteVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licensePlate;
    private String fipeCode;
    private String yearId;

    @Column(precision = 19, scale = 2)
    private BigDecimal fipeValue;

    @Column(precision = 19, scale = 2)
    private BigDecimal coverageLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal calculatedPremium;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    public QuoteVehicle() {
    }

    public QuoteVehicle(Long id, String licensePlate, String fipeCode, String yearId, BigDecimal fipeValue, BigDecimal coverageLimit, BigDecimal calculatedPremium, Quote quote) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.fipeCode = fipeCode;
        this.yearId = yearId;
        this.fipeValue = fipeValue;
        this.coverageLimit = coverageLimit;
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

    public BigDecimal getFipeValue() {
        return fipeValue;
    }

    public void setFipeValue(BigDecimal fipeValue) {
        this.fipeValue = fipeValue;
    }

    public BigDecimal getCoverageLimit() {
        return coverageLimit;
    }

    public void setCoverageLimit(BigDecimal coverageLimit) {
        this.coverageLimit = coverageLimit;
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
}