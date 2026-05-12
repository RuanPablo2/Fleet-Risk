package com.RuanPablo2.fleet_document_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
    public TopicExchange quoteExchange() {
        return new TopicExchange(EXCHANGE_QUOTE);
    }

    @Bean
    public Binding documentBinding(Queue documentQueue, TopicExchange quoteExchange) {
        return BindingBuilder.bind(documentQueue)
                .to(quoteExchange)
                .with(ROUTING_KEY_APPROVED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}