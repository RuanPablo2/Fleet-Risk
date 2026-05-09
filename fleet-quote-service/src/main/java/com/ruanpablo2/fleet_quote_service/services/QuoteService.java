package com.ruanpablo2.fleet_quote_service.services;

import com.ruanpablo2.fleet_common.dtos.*;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteResponse;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.QuoteVehicle;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import com.ruanpablo2.fleet_quote_service.repositories.QuoteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public QuoteService(QuoteRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Quote createInitialQuote(QuoteRequest request) {
        Quote quote = new Quote();
        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());
        quote.setBrokerName(request.brokerName());
        quote.setStatus(QuoteStatus.PENDING);

        for (QuoteVehicleRequest vehicleReq : request.vehicles()) {
            QuoteVehicle vehicle = new QuoteVehicle();
            vehicle.setLicensePlate(vehicleReq.licensePlate());
            vehicle.setFipeCode(vehicleReq.fipeCode());
            vehicle.setYearId(vehicleReq.yearId());
            vehicle.setCoverageLimit(vehicleReq.coverageLimit());

            quote.addVehicle(vehicle);
        }

        Quote savedQuote = repository.save(quote);

        List<QuoteVehicleEventDTO> vehicleEvents = savedQuote.getVehicles().stream()
                .map(v -> new QuoteVehicleEventDTO(
                        v.getId(),
                        v.getFipeCode(),
                        v.getYearId(),
                        v.getCoverageLimit()
                ))
                .toList();

        QuoteCreatedEventDTO event = new QuoteCreatedEventDTO(
                savedQuote.getId(),
                vehicleEvents
        );

        System.out.println("📤 Publishing calculation request for Quote ID: " + savedQuote.getId() + " | Fleet size: " + vehicleEvents.size());

        rabbitTemplate.convertAndSend(
                "fleet.quote.events",
                "quote.created.key",
                event
        );

        return savedQuote;
    }

    @Transactional
    public QuoteResponse updateQuote(Long id, QuoteRequest request) {
        Quote quote = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found: " + id));

        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());
        quote.setBrokerName(request.brokerName());
        quote.setStatus(QuoteStatus.PENDING);
        quote.setTotalPremium(null);

        quote.getVehicles().clear();

        request.vehicles().forEach(v -> {
            QuoteVehicle vehicle = new QuoteVehicle(
                    v.licensePlate(),
                    v.fipeCode(),
                    v.yearId(),
                    v.coverageLimit()
            );
            quote.addVehicle(vehicle);
        });

        Quote savedQuote = repository.save(quote);

        List<QuoteVehicleEventDTO> vehicleDTOs = savedQuote.getVehicles().stream()
                .map(v -> new QuoteVehicleEventDTO(
                        v.getId(),
                        v.getFipeCode(),
                        v.getYearId(),
                        v.getCoverageLimit()
                ))
                .toList();

        QuoteCreatedEventDTO event = new QuoteCreatedEventDTO(savedQuote.getId(), vehicleDTOs);
        rabbitTemplate.convertAndSend("fleet.quote.events", "quote.created.key", event);

        System.out.println("🔄 [RECALCULATION] Quote " + id + " updated and sent to Pricing Service.");

        return new QuoteResponse(
                savedQuote.getId(),
                savedQuote.getCustomerName(),
                savedQuote.getCustomerCnpj(),
                savedQuote.getBrokerName(),
                savedQuote.getTotalPremium(),
                savedQuote.getStatus().name()
        );
    }

    @Transactional
    public void updateCalculatedPrices(QuoteCalculatedEventDTO event) {
        Quote quote = repository.findById(event.quoteId())
                .orElseThrow(() -> new RuntimeException("Quote not found: " + event.quoteId()));

        quote.setTotalPremium(event.totalPremium());
        quote.setStatus(QuoteStatus.CALCULATED);

        for (QuoteVehicleCalculatedEventDTO vehicleEvent : event.vehicles()) {
            for (QuoteVehicle vehicle : quote.getVehicles()) {
                if (vehicle.getId().equals(vehicleEvent.vehicleId())) {
                    vehicle.setCalculatedPremium(vehicleEvent.calculatedPremium());
                }
            }
        }

        repository.save(quote);
        System.out.println("✅ Quote ID: " + quote.getId() + " successfully updated with prices!");
    }

    public Quote getQuoteById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found with ID: " + id));
    }
}