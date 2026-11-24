package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.NotificationCountResponseDto;
import com.decisionhub.backend.dto.NotificationResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.service.NotificationService;
import com.decisionhub.backend.util.AppConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for managing user notifications, unread counts, and read status updates.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieve all paginated notifications for the authenticated user (newest first).
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<NotificationResponseDto>>> getUserNotifications(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponseDto<NotificationResponseDto> response =
                notificationService.getUserNotifications(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Notifications retrieved successfully", response));
    }

    /**
     * Retrieve only unread paginated notifications for the authenticated user.
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<NotificationResponseDto>>> getUnreadNotifications(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponseDto<NotificationResponseDto> response =
                notificationService.getUnreadNotifications(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Unread notifications retrieved successfully", response));
    }

    /**
     * Get the total unread notification count for the authenticated user.
     */
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponseDto<NotificationCountResponseDto>> getUnreadCount(
            Authentication authentication) {
        NotificationCountResponseDto response =
                notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Unread count retrieved successfully", response));
    }

    /**
     * Mark a single notification as read (recipient only).
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponseDto<NotificationResponseDto>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        NotificationResponseDto response =
                notificationService.markAsRead(notificationId, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Notification marked as read", response));
    }

    /**
     * Mark all unread notifications as read for the authenticated user.
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponseDto<NotificationCountResponseDto>> markAllAsRead(
            Authentication authentication) {
        NotificationCountResponseDto response =
                notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("All notifications marked as read", response));
    }

    /**
     * Delete a single notification (recipient only).
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {
        notificationService.deleteNotification(notificationId, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Notification deleted successfully", null));
    }
}
