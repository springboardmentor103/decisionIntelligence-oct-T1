package com.decisionhub.backend.dto;

/**
 * Overview analytics summary distinguishing global administrators from ordinary users.
 */
public record AnalyticsOverviewResponseDto(
    long totalDecisions,
    long activeDecisions,
    long closedDecisions,
    long totalCommunities,
    Long totalRegisteredUsers,
    long totalBallots,
    long totalComments,
    long totalFeedback,
    boolean isGlobalAdmin
) {
}
