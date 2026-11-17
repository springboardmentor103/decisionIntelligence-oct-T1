package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ComparisonDtos;
import com.decisionhub.backend.service.ComparisonService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService comparisonService;

    // Add comparison criteria
    @PostMapping("/decisions/{decisionId}/comparison/criteria")
    @ResponseStatus(HttpStatus.CREATED)
    public ComparisonDtos.CriteriaResponse addCriteria(
            @PathVariable Long decisionId,
            @RequestBody ComparisonDtos.CreateCriteriaRequest request,
            Principal principal
    ) {
        return comparisonService.addCriteria(
                decisionId, request, principal.getName()
        );
    }

    // Retrieve comparison details
    @GetMapping("/decisions/{decisionId}/comparison")
    public ComparisonDtos.ComparisonDetailsResponse getComparisonDetails(
            @PathVariable Long decisionId,
            Principal principal
    ) {
        return comparisonService.getComparisonDetails(
                decisionId, principal.getName()
        );
    }

    // Add pros or cons
    @PostMapping("/options/{optionId}/pros-cons")
    @ResponseStatus(HttpStatus.CREATED)
    public ComparisonDtos.ProsConsResponse addProsCons(
            @PathVariable Long optionId,
            @RequestBody ComparisonDtos.AddProsConsRequest request,
            Principal principal
    ) {
        return comparisonService.addProsCons(
                optionId, request, principal.getName()
        );
    }

    // Assign score to an option for a criterion
    @PostMapping("/options/{optionId}/scores")
    public ComparisonDtos.ScoreResponse addScore(
            @PathVariable Long optionId,
            @RequestBody ComparisonDtos.AddScoreRequest request,
            Principal principal
    ) {
        return comparisonService.addScore(
                optionId, request, principal.getName()
        );
    }

    // Retrieve ranked options
    @GetMapping("/decisions/{decisionId}/comparison/ranking")
    public List<ComparisonDtos.RankedOptionResponse> getRankedOptions(
            @PathVariable Long decisionId,
            Principal principal
    ) {
        return comparisonService.getRankedOptions(
                decisionId, principal.getName()
        );
    }
}