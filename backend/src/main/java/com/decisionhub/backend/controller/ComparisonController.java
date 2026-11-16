package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.ComparisonMatrixResponseDto;
import com.decisionhub.backend.dto.CreateCriteriaRequest;
import com.decisionhub.backend.dto.CreateProConRequest;
import com.decisionhub.backend.dto.CriteriaResponseDto;
import com.decisionhub.backend.dto.OptionComparisonRequest;
import com.decisionhub.backend.dto.OptionComparisonResponseDto;
import com.decisionhub.backend.dto.OptionScoreBatchRequest;
import com.decisionhub.backend.dto.OptionScoreResponseDto;
import com.decisionhub.backend.dto.ProConResponseDto;
import com.decisionhub.backend.dto.RankedOptionDto;
import com.decisionhub.backend.dto.UpdateCriteriaRequest;
import com.decisionhub.backend.dto.UpdateProConRequest;
import com.decisionhub.backend.service.ComparisonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decisions/{decisionId}")
public class ComparisonController {

    private final ComparisonService comparisonService;

    public ComparisonController(ComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    // =========================================================================
    // 1. Comparison Criteria Endpoints
    // =========================================================================

    @PostMapping("/criteria")
    public ResponseEntity<ApiResponseDto<CriteriaResponseDto>> createCriteria(
            @PathVariable Long decisionId,
            @Valid @RequestBody CreateCriteriaRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        CriteriaResponseDto created = comparisonService.createCriteria(decisionId, request, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Comparison criterion created successfully", created));
    }

    @PostMapping("/criteria/default")
    public ResponseEntity<ApiResponseDto<List<CriteriaResponseDto>>> createDefaultCriteria(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<CriteriaResponseDto> defaults = comparisonService.createDefaultCriteria(decisionId, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Default comparison criteria initialized successfully", defaults));
    }

    @GetMapping("/criteria")
    public ResponseEntity<ApiResponseDto<List<CriteriaResponseDto>>> getCriteria(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<CriteriaResponseDto> criteria = comparisonService.getCriteriaByDecisionId(decisionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Comparison criteria retrieved successfully", criteria));
    }

    @PutMapping("/criteria/{criteriaId}")
    public ResponseEntity<ApiResponseDto<CriteriaResponseDto>> updateCriteria(
            @PathVariable Long decisionId,
            @PathVariable Long criteriaId,
            @Valid @RequestBody UpdateCriteriaRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        CriteriaResponseDto updated = comparisonService.updateCriteria(decisionId, criteriaId, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Comparison criterion updated successfully", updated));
    }

    @DeleteMapping("/criteria/{criteriaId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCriteria(
            @PathVariable Long decisionId,
            @PathVariable Long criteriaId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        comparisonService.deleteCriteria(decisionId, criteriaId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Comparison criterion deleted successfully", null));
    }

    // =========================================================================
    // 2. Qualitative Option Comparison Data Endpoints
    // =========================================================================

    @PutMapping("/options/{optionId}/comparison")
    public ResponseEntity<ApiResponseDto<OptionComparisonResponseDto>> upsertOptionComparison(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @RequestBody OptionComparisonRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        OptionComparisonResponseDto saved = comparisonService.upsertOptionComparison(decisionId, optionId, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Option comparison data saved successfully", saved));
    }

    @GetMapping("/options/{optionId}/comparison")
    public ResponseEntity<ApiResponseDto<OptionComparisonResponseDto>> getOptionComparison(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        OptionComparisonResponseDto comparison = comparisonService.getOptionComparison(decisionId, optionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Option comparison data retrieved successfully", comparison));
    }

    // =========================================================================
    // 3. Option Scores Endpoints
    // =========================================================================

    @PostMapping("/options/{optionId}/scores")
    public ResponseEntity<ApiResponseDto<List<OptionScoreResponseDto>>> setOptionScores(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @Valid @RequestBody OptionScoreBatchRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<OptionScoreResponseDto> saved = comparisonService.setOptionScores(decisionId, optionId, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Option scores saved successfully", saved));
    }

    @GetMapping("/options/{optionId}/scores")
    public ResponseEntity<ApiResponseDto<List<OptionScoreResponseDto>>> getOptionScores(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<OptionScoreResponseDto> scores = comparisonService.getOptionScores(decisionId, optionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Option scores retrieved successfully", scores));
    }

    // =========================================================================
    // 4. Pros and Cons Endpoints
    // =========================================================================

    @PostMapping("/options/{optionId}/pros-cons")
    public ResponseEntity<ApiResponseDto<ProConResponseDto>> addProCon(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @Valid @RequestBody CreateProConRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        ProConResponseDto created = comparisonService.addProCon(decisionId, optionId, request, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Pro/Con item added successfully", created));
    }

    @GetMapping("/options/{optionId}/pros-cons")
    public ResponseEntity<ApiResponseDto<List<ProConResponseDto>>> getProsAndCons(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<ProConResponseDto> list = comparisonService.getProsAndCons(decisionId, optionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Pros and cons retrieved successfully", list));
    }

    @PutMapping("/options/{optionId}/pros-cons/{proConId}")
    public ResponseEntity<ApiResponseDto<ProConResponseDto>> updateProCon(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @PathVariable Long proConId,
            @Valid @RequestBody UpdateProConRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        ProConResponseDto updated = comparisonService.updateProCon(decisionId, optionId, proConId, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Pro/Con item updated successfully", updated));
    }

    @DeleteMapping("/options/{optionId}/pros-cons/{proConId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProCon(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @PathVariable Long proConId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        comparisonService.deleteProCon(decisionId, optionId, proConId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Pro/Con item deleted successfully", null));
    }

    // =========================================================================
    // 5. Evaluation Matrix & Rankings Endpoints
    // =========================================================================

    @GetMapping("/comparison-matrix")
    public ResponseEntity<ApiResponseDto<ComparisonMatrixResponseDto>> getComparisonMatrix(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        ComparisonMatrixResponseDto matrix = comparisonService.getComparisonMatrix(decisionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Comparison matrix generated successfully", matrix));
    }

    @GetMapping("/rankings")
    public ResponseEntity<ApiResponseDto<List<RankedOptionDto>>> getRankings(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<RankedOptionDto> rankings = comparisonService.getRankedOptions(decisionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Ranked options retrieved successfully", rankings));
    }
}
