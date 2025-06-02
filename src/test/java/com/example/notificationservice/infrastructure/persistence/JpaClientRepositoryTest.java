package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaClientRepositoryTest {

    @Mock
    private SpringDataClientRepository springDataClientRepository;

    private JpaClientRepository jpaClientRepository;

    @BeforeEach
    void setUp() {
        jpaClientRepository = new JpaClientRepository(springDataClientRepository);
    }

    @Test
    void testFindById_WhenClientExists() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        Client expectedClient = Client.builder()
                .id(clientId)
                .name("Test Client")
                .webhookUrl("https://example.com/webhook")
                .build();

        when(springDataClientRepository.findById(clientId))
                .thenReturn(Optional.of(expectedClient));

        // Act
        Optional<Client> result = jpaClientRepository.findById(clientId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedClient, result.get());
        verify(springDataClientRepository, times(1)).findById(clientId);
    }

    @Test
    void testFindById_WhenClientDoesNotExist() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        when(springDataClientRepository.findById(clientId))
                .thenReturn(Optional.empty());

        // Act
        Optional<Client> result = jpaClientRepository.findById(clientId);

        // Assert
        assertTrue(result.isEmpty());
        verify(springDataClientRepository, times(1)).findById(clientId);
    }
} 