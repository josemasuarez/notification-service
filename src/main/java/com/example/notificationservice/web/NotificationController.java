package com.example.notificationservice.web;

import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationEvent> createNotification(
            @RequestHeader(CORRELATION_ID_HEADER) String correlationId,
            @Valid @RequestBody NotificationRequest request) {

        try {
            setCorrelationId(correlationId);
            log.info("Processing notification request for client {} and event {}", request.getClientId(), request.getEventType());

            return notificationService.processNotificationRequest(request)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        log.info("Client {} is not subscribed to event {}", request.getClientId(), request.getEventType());
                        return ResponseEntity.noContent().build();
                    });

        } finally {
            clearCorrelationId();
        }
    }

    private void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
    }

    private void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }
}
