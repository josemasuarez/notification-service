package com.example.notificationservice.domain.port;

import com.example.notificationservice.domain.model.NotificationEvent;

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);
} 