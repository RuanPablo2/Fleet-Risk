package com.RuanPablo2.fleet_pricing_service.consumers;

import com.ruanpablo2.fleet_common.dtos.QuoteCreatedEventDTO;
import com.ruanpablo2.fleet_common.dtos.QuoteVehicleEventDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QuoteCreatedConsumer {

    @RabbitListener(queues = "quote.created.queue")
    public void processQuoteCalculation(QuoteCreatedEventDTO event) {
        System.out.println("📥 Received calculation request for Quote ID: " + event.quoteId());
        System.out.println("🚗 Vehicles in fleet: " + event.vehicles().size());

        for(QuoteVehicleEventDTO vehicle : event.vehicles()) {
            System.out.println("   -> Vehicle ID: " + vehicle.vehicleId() + " | FIPE: " + vehicle.fipeCode());
        }
    }
}