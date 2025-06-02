package com.example.notificationservice.domain.port;

import java.util.UUID;

public interface SubscriptionRepository {

    boolean existsActiveSubscription(UUID clientId, String eventTypeName);
} 