package com.RuanPablo2.fleet_document_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_DOCUMENT_GENERATE = "fleet.document.generate.queue";
    public static final String EXCHANGE_QUOTE = "fleet.quote.events";
    public static final String ROUTING_KEY_APPROVED = "quote.approved.key";

    @Bean
    public Queue documentQueue() {
        return new Queue(QUEUE_DOCUMENT_GENERATE, true);
    }

    @Bean
    public DirectExchange quoteExchange() {
        return new DirectExchange(EXCHANGE_QUOTE);
    }

    @Bean
    public Binding documentBinding(Queue documentQueue, DirectExchange quoteExchange) {
        return BindingBuilder.bind(documentQueue)
                .to(quoteExchange)
                .with(ROUTING_KEY_APPROVED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}