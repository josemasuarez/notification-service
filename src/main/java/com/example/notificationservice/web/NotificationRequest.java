package com.example.notificationservice.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
    
    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotNull(message = "Payload is required")
    private JsonNode payload;
} 