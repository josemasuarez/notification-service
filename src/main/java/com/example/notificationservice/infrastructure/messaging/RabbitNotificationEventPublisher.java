package com.example.notificationservice.infrastructure.messaging;

import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.port.NotificationEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitNotificationEventPublisher implements NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_NAME = "webhook.delivery";

    @Override
    public void publish(NotificationEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(QUEUE_NAME, message);
            log.info("Event published to queue {}: {}", QUEUE_NAME, event.getId());
        } catch (Exception e) {
            log.error("Error publishing event to queue {}: {}", QUEUE_NAME, event.getId(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
} 