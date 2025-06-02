package com.example.notificationservice.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionTest {

    private Client testClient;
    private EventType testEventType;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testClient.onCreate();

        testEventType = EventType.builder()
                .id(UUID.randomUUID())
                .name("TEST_EVENT")
                .description("Test Event Type")
                .build();
    }

    @Test
    void testCreateSubscription() {
        Subscription subscription = Subscription.builder()
                .client(testClient)
                .eventType(testEventType)
                .active(true)
                .build();

        // Inicializamos las fechas
        subscription.onCreate();

        assertNotNull(subscription);
        assertEquals(testClient, subscription.getClient());
        assertEquals(testEventType, subscription.getEventType());
        assertTrue(subscription.isActive());
        assertNotNull(subscription.getCreatedAt());
        assertNotNull(subscription.getUpdatedAt());
    }

    @Test
    void testPrePersist() {
        Subscription subscription = new Subscription();
        subscription.setClient(testClient);
        subscription.setEventType(testEventType);
        subscription.setActive(true);

        subscription.onCreate();

        assertNotNull(subscription.getCreatedAt());
        assertNotNull(subscription.getUpdatedAt());
        // Verificamos que las fechas son iguales sin comparar los milisegundos
        assertEquals(
            subscription.getCreatedAt().withNano(0),
            subscription.getUpdatedAt().withNano(0)
        );
    }

    @Test
    void testPreUpdate() {
        Subscription subscription = Subscription.builder()
                .client(testClient)
                .eventType(testEventType)
                .active(true)
                .build();

        // Inicializamos las fechas
        subscription.onCreate();
        
        // Verificamos que las fechas se inicializaron correctamente
        assertNotNull(subscription.getCreatedAt());
        assertNotNull(subscription.getUpdatedAt());
        
        LocalDateTime originalUpdatedAt = subscription.getUpdatedAt();
        
        // Simular una actualización
        subscription.setActive(false);
        subscription.onUpdate();

        // Verificamos que la fecha de actualización es posterior
        assertNotNull(subscription.getUpdatedAt());
        assertTrue(subscription.getUpdatedAt().isAfter(originalUpdatedAt));
    }
} 