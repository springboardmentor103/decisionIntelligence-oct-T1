package com.decisionhub.backend.dto;

import java.util.List;

public class PollResultDtos {

    public record OptionResult(
            Long optionId,
            String optionTitle,
            long voteCount,
            double votePercentage,
            Double averageRating
    ) {}

    public record PollResultResponse(
            Long decisionId,
            String decisionTitle,
            String pollType,
            long totalBallots,
            long totalSelections,
            List<Long> winnerOptionIds,
            List<OptionResult> optionResults
    ) {}
}