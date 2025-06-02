package com.example.notificationservice.domain.port;

import com.example.notificationservice.domain.model.NotificationEvent;
import java.util.List;
import java.util.UUID;

public interface NotificationEventRepository {
    NotificationEvent save(NotificationEvent event);
    NotificationEvent findById(UUID id);
    List<NotificationEvent> findAll();
} 