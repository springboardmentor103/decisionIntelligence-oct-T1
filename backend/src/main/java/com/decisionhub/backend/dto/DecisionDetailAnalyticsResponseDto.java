package com.decisionhub.backend.dto;

import java.util.List;

/**
 * Detailed analytics for an individual decision board and poll.
 */
public record DecisionDetailAnalyticsResponseDto(
    Long decisionId,
    String title,
    String category,
    String visibility,
    String status,
    String pollType,
    long totalBallots,
    long totalSelections,
    long totalComments,
    long totalFeedback,
    List<OptionVoteAnalyticsDto> options
) {
}
