package com.example.notificationservice.domain.service;

import com.example.notificationservice.domain.model.EventType;
import com.example.notificationservice.domain.port.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionValidator {

    private final SubscriptionRepository subscriptionRepository;

    public boolean isSubscribed(UUID clientId, String eventTypeName) {
        return subscriptionRepository.existsActiveSubscription(clientId, eventTypeName);
    }
} 