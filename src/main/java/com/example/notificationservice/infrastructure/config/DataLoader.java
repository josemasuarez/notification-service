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
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final SpringDataClientRepository clientRepository;
    private final SpringDataEventTypeRepository eventTypeRepository;
    private final SpringDataSubscriptionRepository subscriptionRepository;
    private final SpringDataNotificationEventRepository notificationEventRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            try {
                notificationEventRepository.deleteAll();
                subscriptionRepository.deleteAll();
                eventTypeRepository.deleteAll();
                clientRepository.deleteAll();
                notificationEventRepository.deleteAll();

                JsonNode root = objectMapper.readTree(
                        new ClassPathResource("notification_events_db.json").getInputStream());

                // Load event types
                List<EventType> eventTypes = loadEventTypes(root.get("eventTypes"));
                Map<String, EventType> eventTypeMap = eventTypes.stream()
                        .collect(Collectors.toMap(EventType::getName, et -> et));

                // Load clients
                List<Client> clients = loadClients(root.get("clients"));
                Map<String, Client> clientMap = clients.stream()
                        .collect(Collectors.toMap(Client::getName, c -> c));

                // Load subscriptions
                loadSubscriptions(root.get("subscriptions"), clientMap, eventTypeMap);

                log.info("Data loaded successfully");
            } catch (Exception e) {
                log.error("Error loading initial data", e);
            }
        };
    }

    private List<EventType> loadEventTypes(JsonNode eventTypesNode) {
        List<EventType> eventTypes = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(eventTypesNode.elements(), 0), false)
                .map(node -> EventType.builder()
                        .name(node.get("name").asText())
                        .description(node.get("description").asText())
                        .build())
                .toList();
        return eventTypeRepository.saveAll(eventTypes);
    }

    private List<Client> loadClients(JsonNode clientsNode) {
        List<Client> clients = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(clientsNode.elements(), 0), false)
                .map(node -> Client.builder()
                        .name(node.get("name").asText())
                        .webhookUrl(node.get("webhookUrl").asText())
                        .build())
                .toList();
        return clientRepository.saveAll(clients);
    }

    private void loadSubscriptions(JsonNode subscriptionsNode, Map<String, Client> clientMap,
            Map<String, EventType> eventTypeMap) {
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(subscriptionsNode.elements(), 0), false)
                .forEach(subscriptionNode -> {
                    String clientName = subscriptionNode.get("clientName").asText();
                    Client client = clientMap.get(clientName);

                    StreamSupport.stream(
                                    Spliterators.spliteratorUnknownSize(subscriptionNode.get("eventTypes").elements(), 0),
                                    false)
                            .forEach(eventTypeNode -> {
                                String eventTypeName = eventTypeNode.asText();
                                EventType eventType = eventTypeMap.get(eventTypeName);

                                Subscription subscription = Subscription.builder()
                                        .client(client)
                                        .eventType(eventType)
                                        .active(true)
                                        .build();

                                subscriptionRepository.save(subscription);
                            });
                });
    }
} 