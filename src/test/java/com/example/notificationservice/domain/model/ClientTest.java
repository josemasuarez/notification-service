package com.example.notificationservice.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void testCreateClient() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();

        assertNotNull(client);
        assertNotNull(client.getId());
        assertEquals("Test Client", client.getName());
        assertEquals("https://example.com/webhook", client.getWebhookUrl());
    }

    @Test
    void testPrePersist() {
        Client client = new Client();
        client.setName("Test Client");
        client.setWebhookUrl("https://example.com/webhook");

        client.onCreate();

        assertNotNull(client.getCreatedAt());
        assertNotNull(client.getUpdatedAt());
        assertEquals(
            client.getCreatedAt().withNano(0),
            client.getUpdatedAt().withNano(0)
        );
    }

    @Test
    void testPreUpdate() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();

        client.onCreate();
        
        assertNotNull(client.getCreatedAt());
        assertNotNull(client.getUpdatedAt());
        
        LocalDateTime originalUpdatedAt = client.getUpdatedAt();
        
        client.setName("Updated Client");
        client.onUpdate();

        assertNotNull(client.getUpdatedAt());
        assertTrue(client.getUpdatedAt().isAfter(originalUpdatedAt));
    }
} 