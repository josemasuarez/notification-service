package com.example.notificationservice.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEventTest {

    private ObjectMapper objectMapper;
    private Client testClient;
    private JsonNode testPayload;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testClient = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testClient.onCreate();
        testPayload = objectMapper.readTree("{\"key\":\"value\"}");
    }

    @Test
    void testCreateNotificationEvent() {
        NotificationEvent event = NotificationEvent.builder()
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        event.onCreate();

        assertNotNull(event);
        assertEquals(testClient, event.getClient());
        assertEquals("TEST_EVENT", event.getEventType());
        assertEquals(testPayload, event.getPayload());
        assertEquals(NotificationStatus.PENDING, event.getStatus());
        assertEquals(0, event.getAttempts());
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
    }

    @Test
    void testPrePersist() {
        NotificationEvent event = new NotificationEvent();
        event.setClient(testClient);
        event.setEventType("TEST_EVENT");
        event.setPayload(testPayload);
        event.setStatus(NotificationStatus.PENDING);

        event.onCreate();

        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
        assertEquals(
            event.getCreatedAt().withNano(0),
            event.getUpdatedAt().withNano(0)
        );
    }

    @Test
    void testPreUpdate() {
        NotificationEvent event = NotificationEvent.builder()
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        event.onCreate();
        
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
        
        LocalDateTime originalUpdatedAt = event.getUpdatedAt();
        
        event.setStatus(NotificationStatus.SUCCESS);
        event.onUpdate();

        assertNotNull(event.getUpdatedAt());
        assertTrue(event.getUpdatedAt().isAfter(originalUpdatedAt));
    }
} 