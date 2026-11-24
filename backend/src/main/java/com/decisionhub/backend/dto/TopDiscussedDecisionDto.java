package com.decisionhub.backend.dto;

/**
 * Summary DTO for most active discussion threads.
 */
public record TopDiscussedDecisionDto(
    Long decisionId,
    String title,
    long commentCount
) {
}
