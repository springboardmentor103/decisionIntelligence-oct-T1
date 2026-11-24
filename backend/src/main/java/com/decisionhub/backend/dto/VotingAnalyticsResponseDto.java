package com.decisionhub.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * Aggregate voting metrics across polls and options.
 */
public record VotingAnalyticsResponseDto(
    long totalBallots,
    long totalSelections,
    long decisionsWithVotingActivity,
    Map<String, Long> byPollType,
    List<OptionVoteAnalyticsDto> optionDistributions
) {
}
