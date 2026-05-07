package com.RuanPablo2.fleet_pricing_service.consumers;

import com.RuanPablo2.fleet_pricing_service.services.PricingService;
import com.ruanpablo2.fleet_common.dtos.QuoteCreatedEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QuoteCreatedConsumer {

    private final PricingService pricingService;

    public QuoteCreatedConsumer(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @RabbitListener(queues = "quote.created.queue")
    public void processQuoteCalculation(QuoteCreatedEventDTO event) {
        pricingService.calculateRisk(event);
    }
}