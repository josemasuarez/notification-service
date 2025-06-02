package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataNotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {
} 