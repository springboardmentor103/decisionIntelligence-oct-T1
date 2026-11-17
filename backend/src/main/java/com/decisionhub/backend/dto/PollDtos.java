package com.decisionhub.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PollDtos {

    public record CreatePollRequest(
            String pollType,
            LocalDateTime deadline
    ) {}

    public record VoteSelectionRequest(
            Long optionId,
            Integer rating
    ) {}

    public record SubmitVoteRequest(
            List<VoteSelectionRequest> selections
    ) {}

    public record OptionVoteCountResponse(
            Long optionId,
            String title,
            long voteCount
    ) {}

    public record PollDetailsResponse(
            Long decisionId,
            String title,
            String pollType,
            String status,
            LocalDateTime deadline,
            boolean expired,
            List<OptionVoteCountResponse> options
    ) {}

    public record VoteStatusResponse(
            Long decisionId,
            boolean hasVoted
    ) {}

    public record SubmitVoteResponse(
            Long ballotId,
            String message
    ) {}
}