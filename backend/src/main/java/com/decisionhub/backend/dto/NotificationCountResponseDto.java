package com.decisionhub.backend.dto;

/**
 * Response DTO containing total unread notification count.
 */
public record NotificationCountResponseDto(
    long unreadCount
) {
}
