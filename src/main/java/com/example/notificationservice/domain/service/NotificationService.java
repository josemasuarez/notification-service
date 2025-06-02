package com.example.notificationservice.domain.service;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.NotificationStatus;
import com.example.notificationservice.domain.port.ClientRepository;
import com.example.notificationservice.domain.port.NotificationEventPublisher;
import com.example.notificationservice.domain.port.NotificationEventRepository;
import com.example.notificationservice.infrastructure.service.IdempotencyService;
import com.example.notificationservice.web.NotificationRequest;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final String CLIENT_NOT_FOUND = "Client not found: ";
    private static final String DUPLICATE_REQUEST = "Duplicate request";

    private final NotificationEventRepository repository;
    private final NotificationEventPublisher publisher;
    private final SubscriptionValidator subscriptionValidator;
    private final IdempotencyService idempotencyService;
    private final ClientRepository clientRepository;


    @Transactional
    public Optional<NotificationEvent> processNotificationRequest(NotificationRequest request) {
        UUID clientId = UUID.fromString(request.getClientId());
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> notFoundException(clientId));

        NotificationEvent event = buildEvent(client, request);

        if (idempotencyService.isDuplicate(event)) {
            log.info("Duplicate notification event detected: {}", event);
            throw new ResponseStatusException(HttpStatus.ACCEPTED, DUPLICATE_REQUEST);
        }

        return processNotificationEvent(event);
    }

    public Optional<NotificationEvent> processNotificationEvent(NotificationEvent event) {
        if (!isSubscribed(event)) {
            log.info("Client {} is not subscribed to event {}", event.getClient().getId(), event.getEventType());
            return Optional.empty();
        }

        NotificationEvent savedEvent = repository.save(event);
        publisher.publish(savedEvent);

        return Optional.of(savedEvent);
    }

    @Transactional
    public void replayEvent(NotificationEvent notificationEvent) {
        if (NotificationStatus.FAILED.equals(notificationEvent.getStatus())) {
            log.info("Replaying failed notification event: {}", notificationEvent);
            processNotificationEvent(notificationEvent);
        } else {
            log.info("Notification event {} is not in FAILED status, cannot replay", notificationEvent);
        }
    }

    private NotificationEvent buildEvent(Client client, NotificationRequest request) {
        return NotificationEvent.builder()
                .client(client)
                .eventType(request.getEventType())
                .payload(request.getPayload())
                .status(NotificationStatus.PENDING)
                .attempts(0)
                .build();
    }

    private boolean isSubscribed(NotificationEvent event) {
        return subscriptionValidator.isSubscribed(event.getClient().getId(), event.getEventType());
    }

    private ResponseStatusException notFoundException(UUID clientId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, CLIENT_NOT_FOUND + clientId);
    }
}
