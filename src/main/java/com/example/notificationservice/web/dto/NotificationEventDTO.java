package com.example.notificationservice.web.dto;

import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.NotificationStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationEventDTO {
    private UUID id;
    private UUID clientId;
    private String eventType;
    private JsonNode payload;
    private NotificationStatus status;
    private Integer attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotificationEventDTO fromEntity(NotificationEvent event) {
        return NotificationEventDTO.builder()
                .id(event.getId())
                .clientId(event.getClient().getId())
                .eventType(event.getEventType())
                .payload(event.getPayload())
                .status(event.getStatus())
                .attempts(event.getAttempts())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
} 