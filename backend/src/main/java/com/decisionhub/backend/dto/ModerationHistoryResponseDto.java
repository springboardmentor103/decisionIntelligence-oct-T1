package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.ModerationHistory;

import java.time.LocalDateTime;

/**
 * Response DTO representing an entry in the moderation audit trail.
 */
public record ModerationHistoryResponseDto(
    Long id,
    Long moderatorId,
    String moderatorName,
    Long communityId,
    Long targetUserId,
    String contentType,
    Long contentId,
    String action,
    String reason,
    Long reportId,
    LocalDateTime createdAt
) {
    public static ModerationHistoryResponseDto fromEntity(ModerationHistory history) {
        if (history == null) {
            return null;
        }

        return new ModerationHistoryResponseDto(
            history.getId(),
            history.getModerator() != null ? history.getModerator().getId() : null,
            history.getModerator() != null ? history.getModerator().getFullName() : null,
            history.getCommunity() != null ? history.getCommunity().getId() : null,
            history.getTargetUser() != null ? history.getTargetUser().getId() : null,
            history.getContentType(),
            history.getContentId(),
            history.getAction(),
            history.getReason(),
            history.getReport() != null ? history.getReport().getId() : null,
            history.getCreatedAt()
        );
    }
}
