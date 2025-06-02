package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaSubscriptionRepository implements SubscriptionRepository {

    private final SpringDataSubscriptionRepository repository;

    @Override
    public boolean existsActiveSubscription(UUID clientId, String eventTypeName) {
        return repository.existsByClientIdAndEventTypeNameAndActiveTrue(clientId, eventTypeName);
    }
} 