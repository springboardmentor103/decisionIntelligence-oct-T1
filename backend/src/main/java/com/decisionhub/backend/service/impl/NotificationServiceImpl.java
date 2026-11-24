package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.NotificationCountResponseDto;
import com.decisionhub.backend.dto.NotificationResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.entity.Notification;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.NotificationRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of NotificationService for managing in-app notifications and read statuses.
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<NotificationResponseDto> getUserNotifications(String userEmail, Pageable pageable) {
        User user = getAuthenticatedUser(userEmail);
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<NotificationResponseDto> dtoList = page.getContent().stream()
                .map(NotificationResponseDto::fromEntity)
                .toList();

        Page<NotificationResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, page.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<NotificationResponseDto> getUnreadNotifications(String userEmail, Pageable pageable) {
        User user = getAuthenticatedUser(userEmail);
        Page<Notification> page = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(user.getId(), false, pageable);

        List<NotificationResponseDto> dtoList = page.getContent().stream()
                .map(NotificationResponseDto::fromEntity)
                .toList();

        Page<NotificationResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, page.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountResponseDto getUnreadCount(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        long count = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        return new NotificationCountResponseDto(count);
    }

    @Override
    public NotificationResponseDto markAsRead(Long notificationId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Notification notification = findNotificationOrThrow(notificationId);

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this notification.");
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification = notificationRepository.save(notification);
        }

        return NotificationResponseDto.fromEntity(notification);
    }

    @Override
    public NotificationCountResponseDto markAllAsRead(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        notificationRepository.markAllAsReadByUserId(user.getId());
        return new NotificationCountResponseDto(0L);
    }

    @Override
    public void deleteNotification(Long notificationId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Notification notification = findNotificationOrThrow(notificationId);

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this notification.");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public Notification createNotification(User recipient, NotificationType type, String message, Long referenceId) {
        if (recipient == null || message == null || message.isBlank()) {
            return null;
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .message(message.trim())
                .isRead(false)
                .referenceId(referenceId)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public Notification createNotification(Long recipientUserId, NotificationType type, String message, Long referenceId) {
        if (recipientUserId == null) {
            return null;
        }

        return userRepository.findById(recipientUserId)
                .map(user -> createNotification(user, type, message, referenceId))
                .orElse(null);
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private User getAuthenticatedUser(String email) {
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Authentication required.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private Notification findNotificationOrThrow(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
    }
}
