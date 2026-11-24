package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Notification;
import com.decisionhub.backend.entity.NotificationType;

import java.time.LocalDateTime;

/**
 * Response DTO representing an in-app notification.
 */
public record NotificationResponseDto(
    Long id,
    Long userId,
    NotificationType type,
    String message,
    boolean isRead,
    Long referenceId,
    LocalDateTime createdAt
) {
    public static NotificationResponseDto fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        return new NotificationResponseDto(
            notification.getId(),
            notification.getUser() != null ? notification.getUser().getId() : null,
            notification.getType(),
            notification.getMessage(),
            notification.isRead(),
            notification.getReferenceId(),
            notification.getCreatedAt()
        );
    }
}
