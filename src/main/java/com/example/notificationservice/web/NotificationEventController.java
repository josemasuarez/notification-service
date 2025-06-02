package com.example.notificationservice.web;

import com.example.notificationservice.domain.model.NotificationEvent;
import com.example.notificationservice.domain.model.NotificationStatus;
import com.example.notificationservice.domain.service.NotificationService;
import com.example.notificationservice.domain.port.NotificationEventRepository;
import com.example.notificationservice.web.dto.NotificationEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notification_events")
@RequiredArgsConstructor
public class NotificationEventController {

    private final NotificationEventRepository notificationEventRepository;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationEventDTO>> getAllNotificationEvents() {
        List<NotificationEvent> events = notificationEventRepository.findAll();
        List<NotificationEventDTO> dtos = events.stream()
                .map(NotificationEventDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{notification_event_id}")
    public ResponseEntity<NotificationEventDTO> getNotificationEvent(
            @PathVariable("notification_event_id") UUID notificationEventId) {
        NotificationEvent event = notificationEventRepository.findById(notificationEventId);
        return event != null 
            ? ResponseEntity.ok(NotificationEventDTO.fromEntity(event)) 
            : ResponseEntity.notFound().build();
    }

    @PostMapping("/{notification_event_id}/replay")
    public ResponseEntity<Void> replayNotificationEvent(
            @PathVariable("notification_event_id") UUID notificationEventId) {
        NotificationEvent event = notificationEventRepository.findById(notificationEventId);
        notificationService.replayEvent(event);
        return ResponseEntity.ok().build();
    }
} 