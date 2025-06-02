package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaSubscriptionRepositoryTest {

    @Mock
    private SpringDataSubscriptionRepository springDataSubscriptionRepository;

    private JpaSubscriptionRepository jpaSubscriptionRepository;
    private Client testClient;
    private EventType testEventType;

    @BeforeEach
    void setUp() {
        jpaSubscriptionRepository = new JpaSubscriptionRepository(springDataSubscriptionRepository);
        testClient = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testEventType = EventType.builder()
                .id(UUID.randomUUID())
                .name("TEST_EVENT")
                .description("Test Event Type")
                .build();
    }

    @Test
    void testExistsActiveSubscription_WhenSubscriptionExists() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        String eventTypeName = "TEST_EVENT";

        when(springDataSubscriptionRepository.existsByClientIdAndEventTypeNameAndActiveTrue(clientId, eventTypeName))
                .thenReturn(true);

        // Act
        boolean result = jpaSubscriptionRepository.existsActiveSubscription(clientId, eventTypeName);

        // Assert
        assertTrue(result);
        verify(springDataSubscriptionRepository, times(1))
                .existsByClientIdAndEventTypeNameAndActiveTrue(clientId, eventTypeName);
    }

    @Test
    void testExistsActiveSubscription_WhenSubscriptionDoesNotExist() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        String eventTypeName = "TEST_EVENT";

        when(springDataSubscriptionRepository.existsByClientIdAndEventTypeNameAndActiveTrue(clientId, eventTypeName))
                .thenReturn(false);

        // Act
        boolean result = jpaSubscriptionRepository.existsActiveSubscription(clientId, eventTypeName);

        // Assert
        assertFalse(result);
        verify(springDataSubscriptionRepository, times(1))
                .existsByClientIdAndEventTypeNameAndActiveTrue(clientId, eventTypeName);
    }
} 