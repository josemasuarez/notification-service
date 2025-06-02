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
        if (idempotencyService.isDuplicate(request)) {
            log.info("Duplicate request for clientId={} and eventType={}", request.getClientId(),
                    request.getEventType());
            throw new ResponseStatusException(HttpStatus.ACCEPTED, DUPLICATE_REQUEST);
        }

        Client client = getClientOrThrow(UUID.fromString(request.getClientId()));
        NotificationEvent event = buildNotificationEvent(client, request);
        return publishIfSubscribed(event);
    }

    @Transactional
    public void replayEvent(NotificationEvent event) {
        if (!NotificationStatus.FAILED.equals(event.getStatus())) {
            log.info("Skipping replay: Event {} is in status {}", event.getId(), event.getStatus());
            return;
        }

        NotificationRequest request = toRequest(event);

        if (idempotencyService.isDuplicate(request)) {
            log.info("Replay blocked: duplicate event for clientId={} and eventType={}", request.getClientId(),
                    request.getEventType());
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_REQUEST);
        }

        log.info("Replaying failed event {}", event.getId());

        event.setStatus(NotificationStatus.PENDING);
        event.setAttempts(0);

        publishIfSubscribed(event);
    }

    private Optional<NotificationEvent> publishIfSubscribed(NotificationEvent event) {
        if (!subscriptionValidator.isSubscribed(event.getClient().getId(), event.getEventType())) {
            log.info("Client {} is not subscribed to event {}", event.getClient().getId(), event.getEventType());
            return Optional.empty();
        }

        NotificationEvent saved = repository.save(event);
        publisher.publish(saved);

        return Optional.of(saved);
    }

    private Client getClientOrThrow(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CLIENT_NOT_FOUND + clientId));
    }

    private NotificationEvent buildNotificationEvent(Client client, NotificationRequest request) {
        return NotificationEvent.builder()
                .client(client)
                .eventType(request.getEventType())
                .payload(request.getPayload())
                .status(NotificationStatus.PENDING)
                .attempts(0)
                .build();
    }

    private NotificationRequest toRequest(NotificationEvent event) {
        return NotificationRequest.builder()
                .clientId(event.getClient().getId().toString())
                .eventType(event.getEventType())
                .payload(event.getPayload())
                .build();
    }
}