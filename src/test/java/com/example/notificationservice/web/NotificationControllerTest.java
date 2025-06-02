package com.example.notificationservice.web;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.NotificationStatus;
import com.example.notificationservice.domain.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private NotificationController controller;
    private ObjectMapper objectMapper;
    private Client testClient;
    private JsonNode testPayload;

    @BeforeEach
    void setUp() throws Exception {
        controller = new NotificationController(notificationService);
        objectMapper = new ObjectMapper();
        testClient = Client.builder()
                .id(UUID.randomUUID())
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testPayload = objectMapper.readTree("{\"key\":\"value\"}");
    }

    @Test
    void testCreateNotification_WhenClientIsSubscribed() {
        // Arrange
        String correlationId = "test-correlation-id";
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClient.getId().toString())
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .build();

        NotificationEvent expectedEvent = NotificationEvent.builder()
                .id(UUID.randomUUID())
                .client(testClient)
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .status(NotificationStatus.PENDING)
                .build();

        when(notificationService.processNotificationRequest(any(NotificationRequest.class)))
                .thenReturn(Optional.of(expectedEvent));

        // Act
        ResponseEntity<NotificationEvent> response = controller.createNotification(correlationId, request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.hasBody());
        assertEquals(expectedEvent, response.getBody());
        verify(notificationService, times(1)).processNotificationRequest(request);
    }

    @Test
    void testCreateNotification_WhenClientIsNotSubscribed() {
        // Arrange
        String correlationId = "test-correlation-id";
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClient.getId().toString())
                .eventType("TEST_EVENT")
                .payload(testPayload)
                .build();

        when(notificationService.processNotificationRequest(any(NotificationRequest.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<NotificationEvent> response = controller.createNotification(correlationId, request);

        // Assert
        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        assertFalse(response.hasBody());
        verify(notificationService, times(1)).processNotificationRequest(request);
    }
} 