package com.skillvault.backend.dtos.Responses;

import com.skillvault.backend.Controllers.Notification;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponseDTO(
        UUID id,
        String message,
        Boolean read,
        String ago
) {
    public NotificationResponseDTO(Notification notification) {
        this(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                formatTimeAgo(notification.getCreatedAt())
        );
    }

    private static String formatTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) return "just now";
        if (minutes < 60) return "about " + minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        if (hours < 24) return "about " + hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (days < 7) return "about " + days + " day" + (days > 1 ? "s" : "") + " ago";

        long weeks = days / 7;
        if (weeks < 4) return "about " + weeks + " week" + (weeks > 1 ? "s" : "") + " ago";

        long months = now.getMonthValue() - createdAt.getMonthValue() + 12L * (now.getYear() - createdAt.getYear());
        if (months < 12) return "about " + months + " month" + (months > 1 ? "s" : "") + " ago";

        long years = now.getYear() - createdAt.getYear();
        return "about " + years + " year" + (years > 1 ? "s" : "") + " ago";
    }
}
