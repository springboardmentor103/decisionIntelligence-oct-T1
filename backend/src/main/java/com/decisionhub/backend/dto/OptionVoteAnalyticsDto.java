package com.decisionhub.backend.dto;

/**
 * Option-wise vote and rating distribution metrics.
 */
public record OptionVoteAnalyticsDto(
    Long optionId,
    String title,
    long voteCount,
    double percentage,
    Double averageRating
) {
}
