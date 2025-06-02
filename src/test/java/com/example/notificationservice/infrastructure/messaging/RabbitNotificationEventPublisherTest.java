package com.example.notificationservice.infrastructure.messaging;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.NotificationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitNotificationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitNotificationEventPublisher publisher;
    private ObjectMapper objectMapper;
    private Client testClient;
    private JsonNode testPayload;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        publisher = new RabbitNotificationEventPublisher(rabbitTemplate, objectMapper);
        testClient = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testPayload = objectMapper.readTree("{\"key\":\"value\"}");
    }

    @Test
    void testPublish_Success() throws Exception {
        // Arrange
        NotificationEvent event = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        String expectedMessage = objectMapper.writeValueAsString(event);

        // Act
        publisher.publish(event);

        // Assert
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("webhook.delivery"), eq(expectedMessage));
    }

    @Test
    void testPublish_WhenSerializationFails() {
        // Arrange
        NotificationEvent event = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        doThrow(new RuntimeException("Serialization error"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> publisher.publish(event));
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("webhook.delivery"), anyString());
    }
} 