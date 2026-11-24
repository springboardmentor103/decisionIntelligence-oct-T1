package com.decisionhub.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * Engagement analytics for discussions and feedback.
 */
public record EngagementAnalyticsResponseDto(
    long totalComments,
    long totalReplies,
    long totalFeedback,
    Map<String, Long> feedbackByType,
    Map<String, Long> feedbackByStatus,
    List<TopDiscussedDecisionDto> mostDiscussedDecisions
) {
}
