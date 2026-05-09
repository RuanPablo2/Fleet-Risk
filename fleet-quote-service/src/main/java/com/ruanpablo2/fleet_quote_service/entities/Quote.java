package com.ruanpablo2.fleet_quote_service.entities;

import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    @Column(name = "customer_cnpj", nullable = false)
    private String customerCnpj;

    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPremium;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteVehicle> vehicles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = QuoteStatus.PENDING;
    }

    public void addVehicle(QuoteVehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.setQuote(this);
    }

    public Quote() {
    }

    public Quote(Long id, String customerName, String customerCnpj, String brokerName, BigDecimal totalPremium, QuoteStatus status, LocalDateTime createdAt, List<QuoteVehicle> vehicles) {
        this.id = id;
        this.customerName = customerName;
        this.customerCnpj = customerCnpj;
        this.brokerName = brokerName;
        this.totalPremium = totalPremium;
        this.status = status;
        this.createdAt = createdAt;
        this.vehicles = vehicles;
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

    public String getCustomerCnpj() {
        return customerCnpj;
    }

    public void setCustomerCnpj(String customerCnpj) {
        this.customerCnpj = customerCnpj;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public List<QuoteVehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<QuoteVehicle> vehicles) {
        this.vehicles = vehicles;
    }
}