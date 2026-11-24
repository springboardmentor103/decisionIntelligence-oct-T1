
package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponse;
import com.decisionhub.backend.dto.OptionRequest;
import com.decisionhub.backend.dto.OptionResponse;

import com.decisionhub.backend.service.DecisionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.decisionhub.backend.dto.ComparisonCriteriaResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    // API 1: Create Decision
    @PostMapping
    public ResponseEntity<ApiResponseDto<DecisionResponse>> createDecision(
            @Valid @RequestBody CreateDecisionRequest request,
            Authentication authentication) {

        DecisionResponse response = decisionService.createDecision(
                request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(
                        "Decision created successfully", response));
    }

    // API 2: Get Public Decisions
    @GetMapping("/public")
    public ResponseEntity<ApiResponseDto<List<DecisionResponse>>> getPublicDecisions() {

        return ResponseEntity.ok(ApiResponseDto.success(
                "Public decisions retrieved successfully",
                decisionService.getPublicDecisions()));
    }
    // API: Get Community Polls
@GetMapping("/community-polls")
public ResponseEntity<ApiResponseDto<List<DecisionResponse>>> getCommunityPolls() {

    return ResponseEntity.ok(ApiResponseDto.success(
            "Community polls retrieved successfully",
            decisionService.getCommunityPolls()));
}

    // API 3: Get My Decisions
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<List<DecisionResponse>>> getMyDecisions(
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponseDto.success(
                "Your decisions retrieved successfully",
                decisionService.getMyDecisions(authentication.getName())));
    }

    // API 4: Get Decision Details
    @GetMapping("/{decisionId}")
    public ResponseEntity<ApiResponseDto<DecisionResponse>> getDecisionById(
            @PathVariable Long decisionId,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponseDto.success(
                "Decision retrieved successfully",
                decisionService.getDecisionById(
                        decisionId, authentication.getName())));
    }

    // API 5: Update Decision
    @PutMapping("/{decisionId}")
    public ResponseEntity<ApiResponseDto<DecisionResponse>> updateDecision(
            @PathVariable Long decisionId,
            @Valid @RequestBody CreateDecisionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponseDto.success(
                "Decision updated successfully",
                decisionService.updateDecision(
                        decisionId, request, authentication.getName())));
    }

    // API 6: Delete Decision
    @DeleteMapping("/{decisionId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteDecision(
            @PathVariable Long decisionId,
            Authentication authentication) {

        decisionService.deleteDecision(
                decisionId, authentication.getName());

        return ResponseEntity.ok(ApiResponseDto.success(
                "Decision deleted successfully", null));
    }

    // API 7: Add Option
    @PostMapping("/{decisionId}/options")
    public ResponseEntity<ApiResponseDto<OptionResponse>> addOption(
            @PathVariable Long decisionId,
            @Valid @RequestBody OptionRequest request,
            Authentication authentication) {

        OptionResponse response = decisionService.addOption(
                decisionId, request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(
                        "Option added successfully", response));
    }

    // API 8: Get Decision Options
    @GetMapping("/{decisionId}/options")
    public ResponseEntity<ApiResponseDto<List<OptionResponse>>> getDecisionOptions(
            @PathVariable Long decisionId,
            Authentication authentication) {

        return ResponseEntity.ok(ApiResponseDto.success(
                "Decision options retrieved successfully",
                decisionService.getDecisionOptions(
                        decisionId, authentication.getName())));
    }
    // API 9: Get Evaluation Criteria
@GetMapping("/{decisionId}/criteria")
public ResponseEntity<ApiResponseDto<List<ComparisonCriteriaResponse>>> getDecisionCriteria(
        @PathVariable Long decisionId,
        Authentication authentication) {

    return ResponseEntity.ok(ApiResponseDto.success(
            "Evaluation criteria retrieved successfully",
            decisionService.getDecisionCriteria(
                    decisionId, authentication.getName())));
}
}
