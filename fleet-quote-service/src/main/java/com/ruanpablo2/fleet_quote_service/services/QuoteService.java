package com.ruanpablo2.fleet_quote_service.services;

import com.ruanpablo2.fleet_common.dtos.*;
import com.ruanpablo2.fleet_common.enums.CoverageType;
import com.ruanpablo2.fleet_common.exceptions.BusinessRuleException;
import com.ruanpablo2.fleet_common.exceptions.ResourceNotFoundException;
import com.ruanpablo2.fleet_common.exceptions.UnauthorizedAccessException;
import com.ruanpablo2.fleet_quote_service.clients.VehicleClient;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteApprovedEventDTO;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteKpiResponse;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteResponse;
import com.ruanpablo2.fleet_quote_service.dtos.QuoteVehicleApprovedDTO;
import com.ruanpablo2.fleet_quote_service.entities.Quote;
import com.ruanpablo2.fleet_quote_service.entities.QuoteVehicle;
import com.ruanpablo2.fleet_quote_service.entities.VehicleCoverage;
import com.ruanpablo2.fleet_quote_service.entities.enums.QuoteStatus;
import com.ruanpablo2.fleet_quote_service.repositories.QuoteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public QuoteService(QuoteRepository repository, RabbitTemplate rabbitTemplate,
                        VehicleClient vehicleClient, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.vehicleClient = vehicleClient;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Quote createInitialQuote(QuoteRequest request, String loggedBrokerName, String brokerEmail) {
        Quote quote = new Quote();
        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());

        quote.setBrokerName(loggedBrokerName);
        quote.setBrokerEmail(brokerEmail);
        quote.setStatus(QuoteStatus.PENDING);

        for (QuoteVehicleRequest vehicleReq : request.vehicles()) {
            QuoteVehicle vehicle = new QuoteVehicle();
            vehicle.setLicensePlate(vehicleReq.licensePlate());
            vehicle.setFipeCode(vehicleReq.fipeCode());
            vehicle.setYearId(vehicleReq.yearId());

            validateBusinessRules(vehicleReq.coverages());

            for (VehicleCoverageRequest covReq : vehicleReq.coverages()) {
                VehicleCoverage coverage = new VehicleCoverage();
                coverage.setType(covReq.type());
                coverage.setFipePercentage(covReq.fipePercentage());
                coverage.setLimitAmount(covReq.limitAmount());

                vehicle.addCoverage(coverage);
            }

            enrichVehicleWithFipeData(vehicle);
            quote.addVehicle(vehicle);
        }

        Quote savedQuote = repository.save(quote);
        System.out.println("💾 [QUOTE SERVICE] Quote saved as draft for: " + savedQuote.getCustomerName());

        return savedQuote;
    }

    @Transactional
    public QuoteResponse updateQuote(Long id, QuoteRequest request, String loggedBrokerName, String brokerEmail) {
        Quote quote = getQuoteById(id, loggedBrokerName);

        quote.setCustomerName(request.customerName());
        quote.setCustomerCnpj(request.customerCnpj());
        quote.setBrokerEmail(brokerEmail);
        quote.setStatus(QuoteStatus.PENDING);
        quote.setTotalPremium(null);

        quote.getVehicles().clear();

        request.vehicles().forEach(v -> {
            QuoteVehicle vehicle = new QuoteVehicle();
            vehicle.setLicensePlate(v.licensePlate());
            vehicle.setFipeCode(v.fipeCode());
            vehicle.setYearId(v.yearId());

            validateBusinessRules(v.coverages());

            v.coverages().forEach(covReq -> {
                VehicleCoverage coverage = new VehicleCoverage();
                coverage.setType(covReq.type());
                coverage.setFipePercentage(covReq.fipePercentage());
                coverage.setLimitAmount(covReq.limitAmount());

                vehicle.addCoverage(coverage);
            });

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

    private void validateBusinessRules(List<VehicleCoverageRequest> coverages) {
        if (coverages == null || coverages.isEmpty()) {
            throw new BusinessRuleException("Vehicle must have at least one selected coverage.", "QUOTE_422");
        }

        boolean hasDM = coverages.stream().anyMatch(c -> c.type() == CoverageType.RCF_DM);
        boolean hasDC = coverages.stream().anyMatch(c -> c.type() == CoverageType.RCF_DC);
        boolean hasDMO = coverages.stream().anyMatch(c -> c.type() == CoverageType.RCF_DMO);

        if (hasDMO && (!hasDM || !hasDC)) {
            throw new BusinessRuleException("To select Moral Damages (DMO), you must also select Material Damages (DM) and Bodily Injury (DC).", "QUOTE_422");
        }
    }

    @Transactional
    public void calculateQuote(Long id, QuoteRequest request, String loggedBrokerName, String brokerEmail) {
        updateQuote(id, request, loggedBrokerName, brokerEmail);

        Quote quote = repository.findById(id).orElseThrow();

        List<QuoteVehicleEventDTO> vehicleEvents = quote.getVehicles().stream()
                .map(v -> {
                    List<VehicleCoverageEventDTO> coverageEvents = v.getCoverages().stream()
                            .map(c -> new VehicleCoverageEventDTO(c.getType(), c.getFipePercentage(), c.getLimitAmount()))
                            .toList();

                    return new QuoteVehicleEventDTO(
                            v.getId(),
                            v.getFipeCode(),
                            v.getYearId(),
                            coverageEvents
                    );
                })
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

                    if (vehicleEvent.coverages() != null) {
                        for (VehicleCoverageCalculatedEventDTO covEvent : vehicleEvent.coverages()) {
                            for (VehicleCoverage coverage : vehicle.getCoverages()) {
                                if (coverage.getType() == covEvent.type()) {
                                    coverage.setPremiumAmount(covEvent.premiumAmount());
                                }
                            }
                        }
                    }
                }
            }
        }

        repository.save(quote);
        System.out.println("✅ [QUOTE SERVICE] Quote ID: " + quote.getId() + " successfully updated with prices and coverage breakdown!");

        messagingTemplate.convertAndSend("/topic/quotes/" + quote.getId(), "CALCULATED");
    }

    public Page<QuoteResponse> listQuotes(String loggedBrokerName, String term, QuoteStatus status, Pageable pageable) {

        String searchTerm = (term == null || term.trim().isEmpty()) ? null : "%" + term.toLowerCase() + "%";

        System.out.println("📊 [QUOTE SERVICE] Listing quotes for broker: " + loggedBrokerName + " | Filters -> term: " + term + ", status: " + status);

        return repository.findQuotesWithFilters(loggedBrokerName, searchTerm, status, pageable)
                .map(quote -> new QuoteResponse(
                        quote.getId(),
                        quote.getCustomerName(),
                        quote.getCustomerCnpj(),
                        quote.getBrokerName(),
                        quote.getTotalPremium(),
                        quote.getStatus().name()
                ));
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
                quote.getBrokerEmail(),
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

    public QuoteKpiResponse getKpis(String loggedBrokerName) {
        long pending = repository.countByBrokerNameAndStatus(loggedBrokerName, QuoteStatus.PENDING);
        long calculated = repository.countByBrokerNameAndStatus(loggedBrokerName, QuoteStatus.CALCULATED);
        long approved = repository.countByBrokerNameAndStatus(loggedBrokerName, QuoteStatus.APPROVED);
        return new QuoteKpiResponse(pending, calculated, approved);
    }
}