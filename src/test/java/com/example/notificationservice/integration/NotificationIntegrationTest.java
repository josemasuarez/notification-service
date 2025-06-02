package com.example.notificationservice.integration;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.EventType;
import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.Subscription;
import com.example.notificationservice.infrastructure.persistence.SpringDataClientRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataEventTypeRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataNotificationEventRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataSubscriptionRepository;
import com.example.notificationservice.web.NotificationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NotificationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SpringDataClientRepository clientRepository;

    @Autowired
    private SpringDataEventTypeRepository eventTypeRepository;

    @Autowired
    private SpringDataSubscriptionRepository subscriptionRepository;

    @Autowired
    private SpringDataNotificationEventRepository notificationEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Client testClient;
    private EventType testEventType;
    private String baseUrl;
    private JsonNode testPayload;

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar la base de datos
        notificationEventRepository.deleteAll();
        subscriptionRepository.deleteAll();
        eventTypeRepository.deleteAll();
        clientRepository.deleteAll();

        // Crear datos de prueba
        testClient = Client.builder()
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();
        testClient = clientRepository.save(testClient);

        testEventType = EventType.builder()
                .name("TEST_EVENT")
                .description("Test Event Type")
                .build();
        testEventType = eventTypeRepository.save(testEventType);

        Subscription subscription = Subscription.builder()
                .client(testClient)
                .eventType(testEventType)
                .active(true)
                .build();
        subscriptionRepository.save(subscription);

        testPayload = objectMapper.readTree("{\"key\":\"value\"}");
        baseUrl = "http://localhost:" + port + "/internal/notifications";
    }

    @Test
    void testCreateNotification_Success() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClient.getId().toString())
                .eventType(testEventType.getName())
                .payload(testPayload)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());
        HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<NotificationEvent> response = restTemplate.postForEntity(
                baseUrl,
                entity,
                NotificationEvent.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testClient.getId(), response.getBody().getClient().getId());
        assertEquals(testEventType.getName(), response.getBody().getEventType());
        assertEquals(testPayload, response.getBody().getPayload());
    }

    @Test
    void testCreateNotification_ClientNotFound() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .clientId(UUID.randomUUID().toString())
                .eventType(testEventType.getName())
                .payload(testPayload)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());
        HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl,
                entity,
                Object.class
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateNotification_NotSubscribed() {
        // Arrange
        EventType unsubscribedEventType = EventType.builder()
                .name("UNSUBSCRIBED_EVENT")
                .description("Unsubscribed Event Type")
                .build();
        eventTypeRepository.save(unsubscribedEventType);

        NotificationRequest request = NotificationRequest.builder()
                .clientId(testClient.getId().toString())
                .eventType(unsubscribedEventType.getName())
                .payload(testPayload)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", UUID.randomUUID().toString());
        HttpEntity<NotificationRequest> entity = new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<Object> response = restTemplate.postForEntity(
                baseUrl,
                entity,
                Object.class
        );

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
} 