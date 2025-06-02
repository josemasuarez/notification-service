package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataSubscriptionRepository extends JpaRepository<Subscription, UUID> {
    boolean existsByClientIdAndEventTypeNameAndActiveTrue(UUID clientId, String eventTypeName);
} 