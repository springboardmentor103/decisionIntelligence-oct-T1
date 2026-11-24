package com.decisionhub.backend.dto;

import java.time.LocalDateTime;

/**
 * Summary DTO describing discussion metrics for a decision.
 */
public record DiscussionSummaryResponseDto(
    Long decisionId,
    long totalComments,
    long totalTopLevelComments,
    long totalReplies,
    LocalDateTime lastActivityAt
) {
}
