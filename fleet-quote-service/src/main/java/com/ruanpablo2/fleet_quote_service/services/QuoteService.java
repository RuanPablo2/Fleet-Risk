package com.ruanpablo2.fleet_quote_service.services;

import com.ruanpablo2.fleet_common.dtos.*;
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
        quote.setStatus(QuoteStatus.PENDING);

        for (QuoteVehicleRequest vehicleReq : request.vehicles()) {
            QuoteVehicle vehicle = new QuoteVehicle();
            vehicle.setLicensePlate(vehicleReq.licensePlate());
            vehicle.setFipeCode(vehicleReq.fipeCode());
            vehicle.setYearId(vehicleReq.yearId());
            vehicle.setFipeValue(vehicleReq.fipeValue());
            vehicle.setCoverageLimit(vehicleReq.coverageLimit());

            quote.addVehicle(vehicle);
        }

        Quote savedQuote = repository.save(quote);

        List<QuoteVehicleEventDTO> vehicleEvents = savedQuote.getVehicles().stream()
                .map(v -> new QuoteVehicleEventDTO(
                        v.getId(),
                        v.getFipeCode(),
                        v.getYearId(),
                        v.getFipeValue(),
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
}