package com.example.notificationservice.infrastructure.persistence;

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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaNotificationEventRepositoryTest {

    @Mock
    private SpringDataNotificationEventRepository repository;

    private JpaNotificationEventRepository jpaNotificationEventRepository;
    private ObjectMapper objectMapper;
    private Client testClient;
    private JsonNode testPayload;

    @BeforeEach
    void setUp() throws Exception {
        jpaNotificationEventRepository = new JpaNotificationEventRepository(repository);
        objectMapper = new ObjectMapper();
        testClient = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testPayload = objectMapper.readTree("{\"key\":\"value\"}");
    }

    @Test
    void testSave() {
        // Arrange
        NotificationEvent event = NotificationEvent.builder()
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        when(repository.save(event)).thenReturn(event);

        // Act
        NotificationEvent result = jpaNotificationEventRepository.save(event);

        // Assert
        assertNotNull(result);
        assertEquals(event, result);
        verify(repository, times(1)).save(event);
    }

    @Test
    void testFindById_WhenEventExists() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        NotificationEvent expectedEvent = NotificationEvent.builder()
                .id(eventId)
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        when(repository.findById(eventId)).thenReturn(Optional.of(expectedEvent));

        // Act
        NotificationEvent result = jpaNotificationEventRepository.findById(eventId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEvent, result);
        verify(repository, times(1)).findById(eventId);
    }

    @Test
    void testFindById_WhenEventDoesNotExist() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        when(repository.findById(eventId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jpaNotificationEventRepository.findById(eventId));
        verify(repository, times(1)).findById(eventId);
    }
} 