package com.ruanpablo2.fleet_quote_service.services;

import com.ruanpablo2.fleet_common.dtos.*;
import com.ruanpablo2.fleet_common.exceptions.BusinessRuleException;
import com.ruanpablo2.fleet_common.exceptions.ResourceNotFoundException;
import com.ruanpablo2.fleet_common.exceptions.UnauthorizedAccessException;
import com.ruanpablo2.fleet_quote_service.clients.VehicleClient;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteApprovedEventDTO;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteResponse;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteVehicleApprovedDTO;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.QuoteVehicle;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import com.ruanpablo2.fleet_quote_service.repositories.QuoteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final VehicleClient vehicleClient;

    public QuoteService(QuoteRepository repository, RabbitTemplate rabbitTemplate, VehicleClient vehicleClient) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.vehicleClient = vehicleClient;
    }

    @Transactional
    public Quote createInitialQuote(QuoteRequest request, String loggedBrokerName) {
        Quote quote = new Quote();
        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());

        quote.setBrokerName(loggedBrokerName);
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
    public QuoteResponse updateQuote(Long id, QuoteRequest request, String loggedBrokerName) {
        Quote quote = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + id, "QUOTE_404"));

        if (!quote.getBrokerName().equals(loggedBrokerName)) {
            throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this quote.", "QUOTE_403");
        }

        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());
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
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + event.quoteId(), "QUOTE_404"));

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

    public Quote getQuoteById(Long id, String loggedBrokerName) {
        Quote quote = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + id, "QUOTE_404"));

        if (!quote.getBrokerName().equals(loggedBrokerName)) {
            throw new UnauthorizedAccessException("Access denied: You do not have permission to view this quote.", "QUOTE_403");
        }

        return quote;
    }

    @Transactional
    public void approveQuote(Long id, String loggedBrokerName) {
        Quote quote = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found with ID: " + id, "QUOTE_404"));

        if (!quote.getBrokerName().equals(loggedBrokerName)) {
            throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this quote.", "QUOTE_403");
        }

        if (quote.getStatus() != QuoteStatus.CALCULATED) {
            throw new BusinessRuleException("Cannot approve a quote that is not in CALCULATED status.", "QUOTE_422");
        }

        quote.setStatus(QuoteStatus.APPROVED);
        repository.save(quote);

        BigDecimal totalFipeCalculated = BigDecimal.ZERO;
        List<QuoteVehicleApprovedDTO> vehicleDTOs = new ArrayList<>();

        for (QuoteVehicle v : quote.getVehicles()) {
            String finalModelName = "Veículo (" + v.getFipeCode() + ")";
            BigDecimal realFipeValue = BigDecimal.ZERO;

            try {
                System.out.println("☁️ [REST-CLIENT] Seeking data from FIPE to enrich the PDF: " + v.getFipeCode());
                var fipeData = vehicleClient.getVehicleDetails(v.getFipeCode(), v.getYearId());

                if (fipeData != null) {
                    finalModelName = fipeData.model() + " (" + v.getFipeCode() + ")";
                    if (fipeData.price() != null) {
                        String cleanPrice = fipeData.price().replace("R$", "").replace(".", "").replace(",", ".").trim();
                        realFipeValue = new BigDecimal(cleanPrice);
                    }
                }
            } catch (Exception e) {
                System.err.println("🚨 Warning: Failed to enrich FIPE data for PDF. Using fallback.");
            }

            totalFipeCalculated = totalFipeCalculated.add(realFipeValue);

            vehicleDTOs.add(new QuoteVehicleApprovedDTO(
                    finalModelName,
                    v.getYearId(),
                    v.getLicensePlate(),
                    realFipeValue,
                    v.getCalculatedPremium()
            ));
        }

        QuoteApprovedEventDTO event = new QuoteApprovedEventDTO(
                quote.getId(),
                quote.getCustomerName(),
                quote.getCustomerCnpj(),
                quote.getBrokerName(),
                quote.getTotalPremium(),
                totalFipeCalculated,
                vehicleDTOs
        );

        System.out.println("✅ [QUOTE SERVICE] Quote " + id + " approved! Sending event to Document Service...");
        rabbitTemplate.convertAndSend("fleet.quote.events", "quote.approved.key", event);
    }
}