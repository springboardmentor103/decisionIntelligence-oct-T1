package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.FeedbackStatus;
import com.decisionhub.backend.entity.FeedbackType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Aggregated summary metrics for feedback on a decision.
 */
public record FeedbackSummaryResponseDto(
    Long decisionId,
    long totalFeedback,
    Map<FeedbackType, Long> countsByType,
    Map<FeedbackStatus, Long> countsByStatus,
    LocalDateTime lastActivityAt
) {
}
