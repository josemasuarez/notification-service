package com.example.notificationservice.infrastructure.persistence;

import com.example.notificationservice.domain.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataEventTypeRepository extends JpaRepository<EventType, UUID> {
} 