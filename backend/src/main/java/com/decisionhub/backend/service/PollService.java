package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.PollDtos;

public interface PollService {

    PollDtos.PollDetailsResponse createPoll(
            Long decisionId,
            PollDtos.CreatePollRequest request,
            String email
    );

    PollDtos.PollDetailsResponse getPollDetails(
            Long decisionId
    );

    PollDtos.SubmitVoteResponse submitVote(
            Long decisionId,
            PollDtos.SubmitVoteRequest request,
            String email
    );

    PollDtos.VoteStatusResponse checkVoteStatus(
            Long decisionId,
            String email
    );
}