package com.skillvault.backend.Controllers;

import com.skillvault.backend.Domain.Notification;
import com.skillvault.backend.Services.NotificationService;
import com.skillvault.backend.Services.TokenService;
import com.skillvault.backend.dtos.Responses.NotificationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final TokenService tokenService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getNotificationsByLoggedUser(Pageable pageable){
        UUID userId = tokenService.getLoggedEntity().getId();
        Page<Notification> notifications = notificationService.getNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(notifications.map(NotificationResponseDTO::new));
    }
}
