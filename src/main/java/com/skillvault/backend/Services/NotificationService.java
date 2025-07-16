package com.skillvault.backend.Services;

import com.skillvault.backend.Domain.Notification;
import com.skillvault.backend.Repositories.NotificationRepository;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.dtos.Requests.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Notification registerNotification(NotificationRequestDTO data){
        Notification notification = Notification.builder()
                .message(data.message())
                .userId(data.userId())
                .build();

        return notificationRepository.save(notification);
    }

    public Page<Notification> getNotificationsByUserId(UUID userId, Pageable pageable){
        if (!userRepository.existsById(userId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID don't exists");
        }
        return notificationRepository.findByUserId(userId, pageable);
    }
}
