package com.RuanPablo2.fleet_pricing_service.services;

import com.ruanpablo2.fleet_common.dtos.QuoteCalculatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteCreatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleCalculatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.02");     // 2% of the FIPE value
    private static final BigDecimal COVERAGE_RATE = new BigDecimal("0.005"); // 0.5% of the Coverage Limit
    private static final BigDecimal AGE_SURCHARGE = new BigDecimal("500.00"); // Aggravation for old cars
    private static final int AGE_THRESHOLD = 2020; // Year limit for exemption from the surcharge

    private final RabbitTemplate rabbitTemplate;

    public PricingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void calculateRisk(QuoteCreatedEventDTO event) {
        System.out.println("🧮 Starting calculation for Quote ID: " + event.quoteId());

        BigDecimal totalFleetPremium = BigDecimal.ZERO;
        List<QuoteVehicleCalculatedEventDTO> calculatedVehicles = new ArrayList<>();

        for (QuoteVehicleEventDTO vehicle : event.vehicles()) {

            BigDecimal basePremium = vehicle.fipeValue().multiply(BASE_RATE);

            BigDecimal coveragePremium = vehicle.coverageLimit().multiply(COVERAGE_RATE);

            BigDecimal ageSurcharge = BigDecimal.ZERO;
            int vehicleYear = extractYear(vehicle.yearId());

            if (vehicleYear < AGE_THRESHOLD) {
                ageSurcharge = AGE_SURCHARGE;
            }

            BigDecimal finalVehiclePremium = basePremium
                    .add(coveragePremium)
                    .add(ageSurcharge)
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