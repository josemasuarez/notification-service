package com.example.notificationservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RabbitConfigTest {

    private final RabbitConfig rabbitConfig = new RabbitConfig();

    @Test
    void testDirectExchange() {
        // Act
        DirectExchange exchange = rabbitConfig.directExchange();

        // Assert
        assertNotNull(exchange);
        assertEquals(RabbitConfig.EXCHANGE, exchange.getName());
        assertTrue(exchange.isDurable());
    }

    @Test
    void testWebhookDeliveryDLQ() {
        // Act
        Queue dlq = rabbitConfig.webhookDeliveryDLQ();

        // Assert
        assertNotNull(dlq);
        assertEquals(RabbitConfig.DLQ_QUEUE, dlq.getName());
        assertTrue(dlq.isDurable());
    }

    @Test
    void testWebhookDeliveryQueue() {
        // Act
        Queue queue = rabbitConfig.webhookDeliveryQueue();

        // Assert
        assertNotNull(queue);
        assertEquals(RabbitConfig.MAIN_QUEUE, queue.getName());
        assertTrue(queue.isDurable());
        assertFalse(queue.isExclusive());
        assertFalse(queue.isAutoDelete());

        // Verify DLQ configuration
        Map<String, Object> args = (Map<String, Object>) ReflectionTestUtils.getField(queue, "arguments");
        assertNotNull(args);
        assertEquals(RabbitConfig.EXCHANGE, args.get("x-dead-letter-exchange"));
        assertEquals(RabbitConfig.DLQ_QUEUE, args.get("x-dead-letter-routing-key"));
    }

    @Test
    void testMainQueueBinding() {
        // Act
        Binding binding = rabbitConfig.mainQueueBinding();

        // Assert
        assertNotNull(binding);
        assertEquals(RabbitConfig.MAIN_QUEUE, binding.getRoutingKey());
        assertEquals(RabbitConfig.MAIN_QUEUE, binding.getDestination());
        assertEquals(RabbitConfig.EXCHANGE, binding.getExchange());
    }

    @Test
    void testDlqBinding() {
        // Act
        Binding binding = rabbitConfig.dlqBinding();

        // Assert
        assertNotNull(binding);
        assertEquals(RabbitConfig.DLQ_QUEUE, binding.getRoutingKey());
        assertEquals(RabbitConfig.DLQ_QUEUE, binding.getDestination());
        assertEquals(RabbitConfig.EXCHANGE, binding.getExchange());
    }
} 