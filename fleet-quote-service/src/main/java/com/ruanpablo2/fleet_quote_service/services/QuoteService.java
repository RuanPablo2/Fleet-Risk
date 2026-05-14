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

            enrichVehicleWithFipeData(vehicle);

            quote.addVehicle(vehicle);
        }

        Quote savedQuote = repository.save(quote);
        System.out.println("💾 [QUOTE SERVICE] Quote saved as draft for: " + savedQuote.getCustomerName());

        return savedQuote;
    }

    @Transactional
    public QuoteResponse updateQuote(Long id, QuoteRequest request, String loggedBrokerName) {
        Quote quote = getQuoteById(id, loggedBrokerName);

        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());
        quote.setStatus(QuoteStatus.PENDING);
        quote.setTotalPremium(null);

        quote.getVehicles().clear();

        request.vehicles().forEach(v -> {
            QuoteVehicle vehicle = new QuoteVehicle();
            vehicle.setLicensePlate(v.licensePlate());
            vehicle.setFipeCode(v.fipeCode());
            vehicle.setYearId(v.yearId());
            vehicle.setCoverageLimit(v.coverageLimit());

            enrichVehicleWithFipeData(vehicle);

            quote.addVehicle(vehicle);
        });

        Quote savedQuote = repository.save(quote);
        System.out.println("🔄 [QUOTE SERVICE] Draft updated for Quote ID: " + id);

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
    public void calculateQuote(Long id, QuoteRequest request, String loggedBrokerName) {
        updateQuote(id, request, loggedBrokerName);

        Quote quote = repository.findById(id).orElseThrow();

        List<QuoteVehicleEventDTO> vehicleEvents = quote.getVehicles().stream()
                .map(v -> new QuoteVehicleEventDTO(
                        v.getId(),
                        v.getFipeCode(),
                        v.getYearId(),
                        v.getCoverageLimit()
                ))
                .toList();

        QuoteCreatedEventDTO event = new QuoteCreatedEventDTO(quote.getId(), vehicleEvents);

        System.out.println("📤 [QUOTE SERVICE] Requesting calculation for Quote ID: " + id);
        rabbitTemplate.convertAndSend("fleet.quote.events", "quote.created.key", event);
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
        System.out.println("✅ [QUOTE SERVICE] Quote ID: " + quote.getId() + " successfully updated with prices!");
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
        Quote quote = getQuoteById(id, loggedBrokerName);

        if (quote.getStatus() != QuoteStatus.CALCULATED) {
            throw new BusinessRuleException("Cannot approve a quote that is not in CALCULATED status.", "QUOTE_422");
        }

        quote.setStatus(QuoteStatus.APPROVED);
        repository.save(quote);

        publishDocumentEvent(quote);
    }

    public void resendDocument(Long id, String loggedBrokerName) {
        Quote quote = getQuoteById(id, loggedBrokerName);

        if (quote.getStatus() != QuoteStatus.APPROVED) {
            throw new BusinessRuleException("Cannot resend document for a quote that is not APPROVED.", "QUOTE_422");
        }

        System.out.println("🔄 [QUOTE SERVICE] Resending document event for Quote ID: " + id);
        publishDocumentEvent(quote);
    }

    private void publishDocumentEvent(Quote quote) {
        BigDecimal totalFipeCalculated = BigDecimal.ZERO;
        List<QuoteVehicleApprovedDTO> vehicleDTOs = new ArrayList<>();

        for (QuoteVehicle v : quote.getVehicles()) {
            BigDecimal fipeValue = v.getFipeValue() != null ? v.getFipeValue() : BigDecimal.ZERO;
            totalFipeCalculated = totalFipeCalculated.add(fipeValue);

            String displayName = v.getModelName() != null ? v.getModelName() : "Vehicle (" + v.getFipeCode() + ")";

            vehicleDTOs.add(new QuoteVehicleApprovedDTO(
                    displayName,
                    v.getYearId(),
                    v.getLicensePlate(),
                    fipeValue,
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

        System.out.println("✅ [QUOTE SERVICE] Event sent to Document Service for Quote " + quote.getId());
        rabbitTemplate.convertAndSend("fleet.quote.events", "quote.approved.key", event);
    }

    private void enrichVehicleWithFipeData(QuoteVehicle vehicle) {
        try {
            System.out.println("☁️ [REST-CLIENT] Fetching FIPE data to freeze value for quote: " + vehicle.getFipeCode());
            var fipeData = vehicleClient.getVehicleDetails(vehicle.getFipeCode(), vehicle.getYearId());

            if (fipeData != null) {
                vehicle.setModelName(fipeData.model() + " (" + vehicle.getFipeCode() + ")");

                if (fipeData.price() != null) {
                    String cleanPrice = fipeData.price().replace("R$", "").replace(".", "").replace(",", ".").trim();
                    vehicle.setFipeValue(new BigDecimal(cleanPrice));
                }
            }
        } catch (Exception e) {
            System.err.println("🚨 Error fetching FIPE data: " + e.getMessage());
            vehicle.setModelName("Vehicle (" + vehicle.getFipeCode() + ")");
            vehicle.setFipeValue(BigDecimal.ZERO);
        }
    }
}