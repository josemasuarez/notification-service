package com.example.notificationservice.infrastructure.service;

import com.example.notificationservice.infrastructure.util.IdempotencyKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private IdempotencyKeyGenerator keyGenerator;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        idempotencyService = new IdempotencyService(redisTemplate, keyGenerator);
    }

    @Test
    void testIsDuplicate_WhenKeyDoesNotExist() {
        // Arrange
        Object payload = new Object();
        String key = "test-key";
        when(keyGenerator.generateKey(payload)).thenReturn(key);
        when(valueOperations.setIfAbsent(eq(key), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        // Act
        boolean result = idempotencyService.isDuplicate(payload);

        // Assert
        assertFalse(result);
        verify(keyGenerator, times(1)).generateKey(payload);
        verify(valueOperations, times(1))
                .setIfAbsent(eq(key), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testIsDuplicate_WhenKeyExists() {
        // Arrange
        Object payload = new Object();
        String key = "test-key";
        when(keyGenerator.generateKey(payload)).thenReturn(key);
        when(valueOperations.setIfAbsent(eq(key), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        // Act
        boolean result = idempotencyService.isDuplicate(payload);

        // Assert
        assertTrue(result);
        verify(keyGenerator, times(1)).generateKey(payload);
        verify(valueOperations, times(1))
                .setIfAbsent(eq(key), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testIsDuplicate_WhenRedisOperationFails() {
        // Arrange
        Object payload = new Object();
        String key = "test-key";
        when(keyGenerator.generateKey(payload)).thenReturn(key);
        when(valueOperations.setIfAbsent(eq(key), eq("1"), eq(60L), eq(TimeUnit.SECONDS)))
                .thenReturn(null);

        // Act
        boolean result = idempotencyService.isDuplicate(payload);

        // Assert
        assertTrue(result);
        verify(keyGenerator, times(1)).generateKey(payload);
        verify(valueOperations, times(1))
                .setIfAbsent(eq(key), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
    }
} 