package com.ruanpablo2.fleet_vehicle_service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_VEHICLE_CONSULTED = "vehicle.consulted.queue";

    public static final String EXCHANGE_VEHICLE = "vehicle.events.exchange";

    public static final String ROUTING_KEY_CONSULTED = "vehicle.consulted.key";

    public static final String QUEUE_SYNC_MODELS = "vehicle.sync.models.queue";
    public static final String ROUTING_KEY_SYNC_MODELS = "vehicle.sync.models.key";

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

    @Bean
    public Queue syncModelsQueue() {
        return QueueBuilder.durable(QUEUE_SYNC_MODELS).build();
    }

    @Bean
    public Binding syncModelsBinding(Queue syncModelsQueue, DirectExchange vehicleExchange) {
        return BindingBuilder.bind(syncModelsQueue)
                .to(vehicleExchange)
                .with(ROUTING_KEY_SYNC_MODELS);
    }
}