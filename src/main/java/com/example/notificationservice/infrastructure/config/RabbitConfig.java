package com.example.notificationservice.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String MAIN_QUEUE = "webhook.delivery";
    public static final String DLQ_QUEUE = "webhook.delivery.dlq";
    public static final String EXCHANGE = "webhook.direct";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue webhookDeliveryDLQ() {
        return new Queue(DLQ_QUEUE, true);
    }

    @Bean
    public Queue webhookDeliveryQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_QUEUE);
        return new Queue(MAIN_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding mainQueueBinding() {
        return BindingBuilder.bind(webhookDeliveryQueue()).to(directExchange()).with(MAIN_QUEUE);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(webhookDeliveryDLQ()).to(directExchange()).with(DLQ_QUEUE);
    }
} 