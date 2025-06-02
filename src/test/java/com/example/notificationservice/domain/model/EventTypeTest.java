package com.example.notificationservice.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventTypeTest {

    @Test
    void testCreateEventType() {
        EventType eventType = EventType.builder()
                .id(UUID.randomUUID())
                .name("TEST_EVENT")
                .description("Test Event Type Description")
                .build();

        assertNotNull(eventType);
        assertNotNull(eventType.getId());
        assertEquals("TEST_EVENT", eventType.getName());
        assertEquals("Test Event Type Description", eventType.getDescription());
    }

    @Test
    void testEventTypeEquality() {
        UUID id = UUID.randomUUID();
        EventType eventType1 = EventType.builder()
                .id(id)
                .name("TEST_EVENT")
                .description("Test Event Type Description")
                .build();

        EventType eventType2 = EventType.builder()
                .id(id)
                .name("TEST_EVENT")
                .description("Test Event Type Description")
                .build();

        assertEquals(eventType1, eventType2);
        assertEquals(eventType1.hashCode(), eventType2.hashCode());
    }

    @Test
    void testEventTypeToString() {
        EventType eventType = EventType.builder()
                .id(UUID.randomUUID())
                .name("TEST_EVENT")
                .description("Test Event Type Description")
                .build();

        String toString = eventType.toString();
        assertTrue(toString.contains("TEST_EVENT"));
        assertTrue(toString.contains("Test Event Type Description"));
    }
} 