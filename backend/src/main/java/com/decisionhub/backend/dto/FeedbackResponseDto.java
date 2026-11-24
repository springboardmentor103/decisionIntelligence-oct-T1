package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Feedback;
import com.decisionhub.backend.entity.FeedbackStatus;
import com.decisionhub.backend.entity.FeedbackType;

import java.time.LocalDateTime;

/**
 * Response DTO representing detailed feedback information.
 */
public record FeedbackResponseDto(
    Long id,
    Long decisionId,
    FeedbackAuthorDto author,
    FeedbackOptionDto option,
    FeedbackType feedbackType,
    String content,
    FeedbackStatus status,
    FeedbackAuthorDto reviewedBy,
    LocalDateTime reviewedAt,
    String statusReason,
    boolean isDeleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FeedbackResponseDto fromEntity(Feedback feedback) {
        if (feedback == null) {
            return null;
        }

        String displayedContent = feedback.isDeleted()
                ? "[This feedback has been deleted]"
                : feedback.getContent();

        return new FeedbackResponseDto(
            feedback.getId(),
            feedback.getDecision() != null ? feedback.getDecision().getId() : null,
            FeedbackAuthorDto.fromEntity(feedback.getAuthor()),
            FeedbackOptionDto.fromEntity(feedback.getOption()),
            feedback.getFeedbackType(),
            displayedContent,
            feedback.getStatus(),
            FeedbackAuthorDto.fromEntity(feedback.getReviewedBy()),
            feedback.getReviewedAt(),
            feedback.getStatusReason(),
            feedback.isDeleted(),
            feedback.getCreatedAt(),
            feedback.getUpdatedAt()
        );
    }
}
