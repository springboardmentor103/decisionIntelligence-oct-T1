package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.NotificationCountResponseDto;
import com.decisionhub.backend.dto.NotificationResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.entity.Notification;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.entity.User;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing in-app notifications and event alerts.
 */
public interface NotificationService {

    PagedResponseDto<NotificationResponseDto> getUserNotifications(String userEmail, Pageable pageable);

    PagedResponseDto<NotificationResponseDto> getUnreadNotifications(String userEmail, Pageable pageable);

    NotificationCountResponseDto getUnreadCount(String userEmail);

    NotificationResponseDto markAsRead(Long notificationId, String userEmail);

    NotificationCountResponseDto markAllAsRead(String userEmail);

    void deleteNotification(Long notificationId, String userEmail);

    Notification createNotification(User recipient, NotificationType type, String message, Long referenceId);

    Notification createNotification(Long recipientUserId, NotificationType type, String message, Long referenceId);
}
