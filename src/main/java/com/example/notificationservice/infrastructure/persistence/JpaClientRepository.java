package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.Client;
import com.example.notificationservice.domain.port.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaClientRepository implements ClientRepository {
    private final SpringDataClientRepository springDataClientRepository;

    @Override
    public Optional<Client> findById(UUID id) {
        return springDataClientRepository.findById(id);
    }
} 