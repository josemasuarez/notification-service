package com.example.notificationservice.infrastructure.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyKeyGeneratorTest {

    private IdempotencyKeyGenerator idempotencyKeyGenerator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        idempotencyKeyGenerator = new IdempotencyKeyGenerator(objectMapper);
    }

    @Test
    void testGenerateKey_WithSimpleObject() {
        // Arrange
        TestObject payload = new TestObject("test", 123);

        // Act
        String key = idempotencyKeyGenerator.generateKey(payload);

        // Assert
        assertNotNull(key);
        assertEquals(64, key.length()); // SHA-256 produces a 64-character hex string
        assertTrue(key.matches("[0-9a-f]{64}")); // Should be a valid hex string
    }

    @Test
    void testGenerateKey_WithSameObject() {
        // Arrange
        TestObject payload1 = new TestObject("test", 123);
        TestObject payload2 = new TestObject("test", 123);

        // Act
        String key1 = idempotencyKeyGenerator.generateKey(payload1);
        String key2 = idempotencyKeyGenerator.generateKey(payload2);

        // Assert
        assertEquals(key1, key2);
    }

    @Test
    void testGenerateKey_WithDifferentObjects() {
        // Arrange
        TestObject payload1 = new TestObject("test1", 123);
        TestObject payload2 = new TestObject("test2", 123);

        // Act
        String key1 = idempotencyKeyGenerator.generateKey(payload1);
        String key2 = idempotencyKeyGenerator.generateKey(payload2);

        // Assert
        assertNotEquals(key1, key2);
    }

    private static class TestObject {
        private String name;
        private int value;

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
    }
} 