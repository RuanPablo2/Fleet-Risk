package com.ruanpablo2.fleet_vehicle_service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_VEHICLE_CONSULTED = "vehicle.consulted.queue";

    public static final String EXCHANGE_VEHICLE = "vehicle.events.exchange";

    public static final String ROUTING_KEY_CONSULTED = "vehicle.consulted.key";

    @Bean
    public DirectExchange vehicleExchange() {
        return new DirectExchange(EXCHANGE_VEHICLE);
    }

    @Bean
    public Queue vehicleConsultedQueue() {
        return QueueBuilder.durable(QUEUE_VEHICLE_CONSULTED).build();
    }

    @Bean
    public Binding vehicleConsultedBinding(Queue vehicleConsultedQueue, DirectExchange vehicleExchange) {
        return BindingBuilder.bind(vehicleConsultedQueue)
                .to(vehicleExchange)
                .with(ROUTING_KEY_CONSULTED);
    }
}