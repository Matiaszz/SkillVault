package com.skillvault.backend.dtos.Requests;

import java.util.UUID;

public record NotificationRequestDTO(
        UUID userId,
        String title,
        String message
) {
}
