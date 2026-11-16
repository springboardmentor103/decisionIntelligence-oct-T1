package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.PollResultDtos;
import com.decisionhub.backend.service.PollResultService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/decisions")
@RequiredArgsConstructor
public class PollResultController {

    private final PollResultService pollResultService;

    @GetMapping("/{decisionId}/poll/results")
    public PollResultDtos.PollResultResponse getPollResults(
            @PathVariable Long decisionId,
            Principal principal
    ) {
        return pollResultService.getPollResults(
                decisionId,
                principal.getName()
        );
    }
}