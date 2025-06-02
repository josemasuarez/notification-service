package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.port.NotificationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaNotificationEventRepository implements NotificationEventRepository {

    private final SpringDataNotificationEventRepository repository;

    @Override
    public NotificationEvent save(NotificationEvent event) {
        return repository.save(event);
    }

    @Override
    public NotificationEvent findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification event not found: " + id));
    }

    @Override
    public List<NotificationEvent> findAll() {
        return repository.findAll();
    }
} 