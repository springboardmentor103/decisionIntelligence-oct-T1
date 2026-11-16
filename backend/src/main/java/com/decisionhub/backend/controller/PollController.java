package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.PollDtos;
import com.decisionhub.backend.service.PollService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/decisions/{decisionId}/poll")
@RequiredArgsConstructor
public class PollController {

    private final PollService pollService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PollDtos.PollDetailsResponse createPoll(
            @PathVariable Long decisionId,
            @RequestBody PollDtos.CreatePollRequest request,
            Principal principal
    ) {
        return pollService.createPoll(
                decisionId, request, principal.getName()
        );
    }

    @GetMapping
    public PollDtos.PollDetailsResponse getPollDetails(
            @PathVariable Long decisionId
    ) {
        return pollService.getPollDetails(decisionId);
    }

    @PostMapping("/vote")
    @ResponseStatus(HttpStatus.CREATED)
    public PollDtos.SubmitVoteResponse submitVote(
            @PathVariable Long decisionId,
            @RequestBody PollDtos.SubmitVoteRequest request,
            Principal principal
    ) {
        return pollService.submitVote(
                decisionId, request, principal.getName()
        );
    }

    @GetMapping("/my-vote")
    public PollDtos.VoteStatusResponse checkVoteStatus(
            @PathVariable Long decisionId,
            Principal principal
    ) {
        return pollService.checkVoteStatus(
                decisionId, principal.getName()
        );
    }
}