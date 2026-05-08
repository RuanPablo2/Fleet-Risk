package com.RuanPablo2.fleet_pricing_service.services;

import com.RuanPablo2.fleet_pricing_service.clients.VehicleClient;
import com.ruanpablo2.fleet_common.dtos.QuoteCalculatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteCreatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleCalculatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.02");
    private static final BigDecimal COVERAGE_RATE = new BigDecimal("0.005");
    private static final BigDecimal AGE_SURCHARGE = new BigDecimal("500.00");
    private static final int AGE_THRESHOLD = 2020;

    private final RabbitTemplate rabbitTemplate;
    private final VehicleClient vehicleClient;

    public PricingService(RabbitTemplate rabbitTemplate, VehicleClient vehicleClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.vehicleClient = vehicleClient;
    }

    public void calculateRisk(QuoteCreatedEventDTO event) {
        System.out.println("🧮 [PRICING] Starting calculation for Quote ID: " + event.quoteId());

        BigDecimal totalFleetPremium = BigDecimal.ZERO;
        List<QuoteVehicleCalculatedEventDTO> calculatedVehicles = new ArrayList<>();

        for (QuoteVehicleEventDTO vehicle : event.vehicles()) {

            BigDecimal realFipeValue = BigDecimal.ZERO;

            try {
                System.out.println("☁️ [FEIGN/REST] Fetching real price for FIPE: " + vehicle.fipeCode());
                var fipeData = vehicleClient.getVehicleDetails(vehicle.fipeCode(), vehicle.yearId());

                if (fipeData != null && fipeData.price() != null) {
                    String cleanPrice = fipeData.price()
                            .replace("R$", "")
                            .replace(".", "")
                            .replace(",", ".")
                            .trim();
                    realFipeValue = new BigDecimal(cleanPrice);
                }
            } catch (HttpClientErrorException.NotFound e) {
                System.err.println("🚨 [WARNING] Car not found in FIPE (404): " + vehicle.fipeCode());
                realFipeValue = BigDecimal.ZERO;
            } catch (Exception e) {
                System.err.println("🚨 [ERROR] Communication failure: " + e.getMessage());
                realFipeValue = BigDecimal.ZERO;
            }

            BigDecimal basePremium = realFipeValue.multiply(BASE_RATE);
            BigDecimal coveragePremium = vehicle.coverageLimit().multiply(COVERAGE_RATE);

            BigDecimal ageSurcharge = BigDecimal.ZERO;
            if (extractYear(vehicle.yearId()) < AGE_THRESHOLD) {
                ageSurcharge = AGE_SURCHARGE;
            }

            BigDecimal finalVehiclePremium = basePremium.add(coveragePremium).add(ageSurcharge)
                    .setScale(2, RoundingMode.HALF_UP);

            calculatedVehicles.add(new QuoteVehicleCalculatedEventDTO(
                    vehicle.vehicleId(),
                    finalVehiclePremium
            ));

            totalFleetPremium = totalFleetPremium.add(finalVehiclePremium);
        }

        System.out.println("💰 Total Fleet Premium for Quote " + event.quoteId() + ": R$ " + totalFleetPremium);

        QuoteCalculatedEventDTO responseEvent = new QuoteCalculatedEventDTO(
                event.quoteId(),
                totalFleetPremium,
                calculatedVehicles
        );

        rabbitTemplate.convertAndSend(
                "fleet.quote.events",
                "quote.calculated.key",
                responseEvent
        );
        System.out.println("📤 Published calculated prices back to RabbitMQ.");
    }

    private int extractYear(String yearId) {
        try {
            String[] parts = yearId.split("-");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 2024;
        }
    }
}