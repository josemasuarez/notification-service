package com.example.notificationservice.domain.port;

import com.example.notificationservice.domain.model.Client;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    Optional<Client> findById(UUID id);
} 