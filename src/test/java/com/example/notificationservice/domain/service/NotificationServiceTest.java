package com.example.notificationservice.domain.service;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.NotificationStatus;
import com.example.notificationservice.domain.port.ClientRepository;
import com.example.notificationservice.domain.port.NotificationEventPublisher;
import com.example.notificationservice.domain.port.NotificationEventRepository;
import com.example.notificationservice.infrastructure.service.IdempotencyService;
import com.example.notificationservice.web.NotificationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationEventRepository repository;
    @Mock
    private NotificationEventPublisher publisher;
    @Mock
    private SubscriptionValidator subscriptionValidator;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private ClientRepository clientRepository;

    private NotificationService notificationService;
    private ObjectMapper objectMapper;
    private Client testClient;
    private JsonNode testPayload;
    private UUID testClientId;
    private String testEventType;

    @BeforeEach
    void setUp() throws Exception {
        notificationService = new NotificationService(
                repository,
                publisher,
                subscriptionValidator,
                idempotencyService,
                clientRepository
        );
        objectMapper = new ObjectMapper();
        testClientId = UUID.randomUUID();
        testEventType = "TEST_EVENT";
        testClient = Client.builder()
                .id(testClientId)
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testPayload = objectMapper.readTree("{\"key\":\"value\"}");
    }

    @Test
    void processNotificationRequest_WhenClientExistsAndSubscribed_ShouldCreateAndPublishEvent() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClientId.toString())
                .eventType(testEventType)
                .payload(testPayload)
                .build();

        when(idempotencyService.isDuplicate(request)).thenReturn(false);
        when(clientRepository.findById(testClientId)).thenReturn(Optional.of(testClient));
        when(subscriptionValidator.isSubscribed(testClientId, testEventType)).thenReturn(true);
        when(repository.save(any(NotificationEvent.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Optional<NotificationEvent> result = notificationService.processNotificationRequest(request);

        // Assert
        assertTrue(result.isPresent());
        NotificationEvent event = result.get();
        assertEquals(testClient, event.getClient());
        assertEquals(testEventType, event.getEventType());
        assertEquals(testPayload, event.getPayload());
        assertEquals(NotificationStatus.PENDING, event.getStatus());
        assertEquals(0, event.getAttempts());

        verify(repository).save(any(NotificationEvent.class));
        verify(publisher).publish(any(NotificationEvent.class));
    }

    @Test
    void processNotificationRequest_WhenClientNotSubscribed_ShouldReturnEmpty() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClientId.toString())
                .eventType(testEventType)
                .payload(testPayload)
                .build();

        when(idempotencyService.isDuplicate(request)).thenReturn(false);
        when(clientRepository.findById(testClientId)).thenReturn(Optional.of(testClient));
        when(subscriptionValidator.isSubscribed(testClientId, testEventType)).thenReturn(false);

        // Act
        Optional<NotificationEvent> result = notificationService.processNotificationRequest(request);

        // Assert
        assertTrue(result.isEmpty());
        verify(repository, never()).save(any(NotificationEvent.class));
        verify(publisher, never()).publish(any(NotificationEvent.class));
    }

    @Test
    void processNotificationRequest_WhenClientNotFound_ShouldThrowException() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClientId.toString())
                .eventType(testEventType)
                .payload(testPayload)
                .build();

        when(idempotencyService.isDuplicate(request)).thenReturn(false);
        when(clientRepository.findById(testClientId)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.processNotificationRequest(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Client not found: " + testClientId, exception.getReason());
    }

    @Test
    void processNotificationRequest_WhenDuplicateRequest_ShouldThrowException() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClientId.toString())
                .eventType(testEventType)
                .payload(testPayload)
                .build();

        when(idempotencyService.isDuplicate(request)).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.processNotificationRequest(request)
        );

        assertEquals(HttpStatus.ACCEPTED, exception.getStatusCode());
        assertEquals("Duplicate request", exception.getReason());
    }

    @Test
    void replayEvent_WhenEventFailed_ShouldReplayEvent() {
        // Arrange
        NotificationEvent event = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .eventType(testEventType)
                .payload(testPayload)
                .status(NotificationStatus.FAILED)
                .attempts(3)
                .build();

        when(idempotencyService.isDuplicate(any(NotificationRequest.class))).thenReturn(false);
        when(subscriptionValidator.isSubscribed(testClientId, testEventType)).thenReturn(true);
        when(repository.save(any(NotificationEvent.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        notificationService.replayEvent(event);

        // Assert
        assertEquals(NotificationStatus.PENDING, event.getStatus());
        assertEquals(0, event.getAttempts());
        verify(repository).save(event);
        verify(publisher).publish(event);
    }

    @Test
    void replayEvent_WhenEventNotFailed_ShouldNotReplay() {
        // Arrange
        NotificationEvent event = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .eventType(testEventType)
                .payload(testPayload)
                .status(NotificationStatus.SUCCESS)
                .attempts(1)
                .build();

        // Act
        notificationService.replayEvent(event);

        // Assert
        assertEquals(NotificationStatus.SUCCESS, event.getStatus());
        assertEquals(1, event.getAttempts());
        verify(repository, never()).save(any(NotificationEvent.class));
        verify(publisher, never()).publish(any(NotificationEvent.class));
    }

    @Test
    void replayEvent_WhenDuplicateRequest_ShouldThrowException() {
        // Arrange
        NotificationEvent event = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .eventType(testEventType)
                .payload(testPayload)
                .status(NotificationStatus.FAILED)
                .attempts(3)
                .build();

        when(idempotencyService.isDuplicate(any(NotificationRequest.class))).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.replayEvent(event)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Duplicate request", exception.getReason());
    }
} 