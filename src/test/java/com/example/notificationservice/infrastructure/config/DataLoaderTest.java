package com.example.notificationservice.infrastructure.config;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.EventType;
import com.example.notificationservice.domain.model.Subscription;
import com.example.notificationservice.infrastructure.persistence.SpringDataClientRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataEventTypeRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataNotificationEventRepository;
import com.example.notificationservice.infrastructure.persistence.SpringDataSubscriptionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private SpringDataClientRepository clientRepository;

    @Mock
    private SpringDataEventTypeRepository eventTypeRepository;

    @Mock
    private SpringDataSubscriptionRepository subscriptionRepository;

    @Mock
    private SpringDataNotificationEventRepository notificationEventRepository;

    private DataLoader dataLoader;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dataLoader = new DataLoader(
                clientRepository,
                eventTypeRepository,
                subscriptionRepository,
                notificationEventRepository,
                objectMapper
        );
    }

    @Test
    void testLoadData() throws Exception {
        // Arrange
        doNothing().when(notificationEventRepository).deleteAll();
        doNothing().when(subscriptionRepository).deleteAll();
        doNothing().when(eventTypeRepository).deleteAll();
        doNothing().when(clientRepository).deleteAll();

        List<EventType> expectedEventTypes = Arrays.asList(
                EventType.builder().name("TEST_EVENT_1").description("Test Event 1").build(),
                EventType.builder().name("TEST_EVENT_2").description("Test Event 2").build()
        );
        when(eventTypeRepository.saveAll(any())).thenReturn(expectedEventTypes);

        List<Client> expectedClients = Arrays.asList(
                Client.builder().name("Client 1").webhookUrl("https://client1.com/webhook").build(),
                Client.builder().name("Client 2").webhookUrl("https://client2.com/webhook").build()
        );
        when(clientRepository.saveAll(any())).thenReturn(expectedClients);

        // Act
        dataLoader.loadData().run();

        // Assert
        verify(notificationEventRepository, times(2)).deleteAll();
        verify(subscriptionRepository, times(1)).deleteAll();
        verify(eventTypeRepository, times(1)).deleteAll();
        verify(clientRepository, times(1)).deleteAll();
        verify(eventTypeRepository, times(1)).saveAll(any());
        verify(clientRepository, times(1)).saveAll(any());
        verify(subscriptionRepository, atLeastOnce()).save(any(Subscription.class));
    }
} 