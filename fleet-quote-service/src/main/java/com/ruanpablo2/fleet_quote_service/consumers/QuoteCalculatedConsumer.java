package com.ruanpablo2.fleet_quote_service.consumers;

import com.ruanpablo2.fleet_common.dtos.QuoteCalculatedEventDTO;
import com.ruanpablo2.fleet_quote_service.services.QuoteService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QuoteCalculatedConsumer {

    private final QuoteService quoteService;

    public QuoteCalculatedConsumer(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @RabbitListener(queues = "quote.calculated.queue")
    public void receiveCalculatedQuote(QuoteCalculatedEventDTO event) {
        System.out.println("📥 Received calculated prices for Quote ID: " + event.quoteId());
        quoteService.updateCalculatedPrices(event);
    }
}