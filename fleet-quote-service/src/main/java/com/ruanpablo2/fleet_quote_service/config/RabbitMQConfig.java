package com.ruanpablo2.fleet_quote_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "fleet.quote.events";
    public static final String QUEUE_QUOTE_CREATED = "quote.created.queue";
    public static final String ROUTING_KEY_QUOTE_CREATED = "quote.created.key";

    @Bean
    public DirectExchange quoteExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue quoteCreatedQueue() {
        return new Queue(QUEUE_QUOTE_CREATED, true); // true = durable (não perde se o RabbitMQ reiniciar)
    }

    @Bean
    public Binding bindingQuoteCreated(Queue quoteCreatedQueue, DirectExchange quoteExchange) {
        return BindingBuilder.bind(quoteCreatedQueue).to(quoteExchange).with(ROUTING_KEY_QUOTE_CREATED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}