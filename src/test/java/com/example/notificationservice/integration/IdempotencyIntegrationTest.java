package com.example.notificationservice.integration;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.EventType;
import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.Subscription;
import com.example.notificationservice.domain.service.NotificationService;
import com.example.notificationservice.infrastructure.persistence.SpringDataClientRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataEventTypeRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataNotificationEventRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataSubscriptionRepository;
import com.example.notificationservice.web.NotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class IdempotencyIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private NotificationService notificationService;

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

    private Client client;
    private EventType eventType;
    private JsonNode payload;

    @BeforeEach
    void setUp() throws Exception {
        // Limpiar datos de prueba anteriores
        notificationEventRepository.deleteAll();
        subscriptionRepository.deleteAll();
        clientRepository.deleteAll();
        eventTypeRepository.deleteAll();
        
        // Crear cliente de prueba
        client = new Client();
        client.setName("Test Client");
        client.setWebhookUrl("https://test-client.com/webhook");
        client = clientRepository.save(client);

        // Crear tipo de evento de prueba
        eventType = new EventType();
        eventType.setName("credit_card_payment");
        eventType.setDescription("Credit Card Payment Event");
        eventType = eventTypeRepository.save(eventType);

        // Crear suscripción
        Subscription subscription = Subscription.builder()
            .client(client)
            .eventType(eventType)
            .active(true)
            .build();
        subscriptionRepository.save(subscription);

        // Crear payload de prueba
        String jsonPayload = """
            {
                "amount": 100.50,
                "currency": "USD",
                "cardLast4": "1234",
                "merchant": "Sample Store"
            }
            """;
        payload = objectMapper.readTree(jsonPayload);
    }

    @Test
    void shouldProcessFirstRequestAndRejectDuplicate() {
        NotificationRequest request1 = NotificationRequest.builder()
            .clientId(client.getId().toString())
            .eventType(eventType.getName())
            .payload(payload)
            .build();

        Optional<NotificationEvent> savedEvent1 = notificationService.processNotificationRequest(request1);
        assertTrue(savedEvent1.isPresent());
        assertNotNull(savedEvent1.get().getId());

        // Segunda solicitud (duplicada)
        NotificationRequest request2 = NotificationRequest.builder()
            .clientId(client.getId().toString())
            .eventType(eventType.getName())
            .payload(payload)
            .build();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> notificationService.processNotificationRequest(request2)
        );

        assertEquals(HttpStatus.ACCEPTED, exception.getStatusCode());
        assertEquals("Duplicate request", exception.getReason());
    }

    @Test
    void shouldProcessDifferentRequests() throws JsonMappingException, JsonProcessingException {
        // Primera solicitud
        NotificationRequest request1 = NotificationRequest.builder()
            .clientId(client.getId().toString())
            .eventType(eventType.getName())
            .payload(payload)
            .build();

        Optional<NotificationEvent> savedEvent1 = notificationService.processNotificationRequest(request1);
        assertTrue(savedEvent1.isPresent());
        assertNotNull(savedEvent1.get().getId());

        // Segunda solicitud (diferente payload)
        JsonNode differentPayload = objectMapper.readTree("""
            {
                "amount": 200.75,
                "currency": "USD",
                "cardLast4": "5678",
                "merchant": "Different Store"
            }
            """);

        NotificationRequest request2 = NotificationRequest.builder()
            .clientId(client.getId().toString())
            .eventType(eventType.getName())
            .payload(differentPayload)
            .build();

        Optional<NotificationEvent> savedEvent2 = notificationService.processNotificationRequest(request2);
        assertTrue(savedEvent2.isPresent());
        assertNotNull(savedEvent2.get().getId());
        assertNotEquals(savedEvent1.get().getId(), savedEvent2.get().getId());
    }
} 