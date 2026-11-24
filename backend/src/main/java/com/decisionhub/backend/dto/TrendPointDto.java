package com.decisionhub.backend.dto;

/**
 * Individual data point representing chronological activity.
 */
public record TrendPointDto(
    String date,
    long decisionsCreated,
    long ballotsSubmitted,
    long commentsPosted,
    long feedbackSubmitted
) {
}
