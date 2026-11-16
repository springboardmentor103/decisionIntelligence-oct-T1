package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponseDto;
import com.decisionhub.backend.dto.OptionBatchRequestDto;
import com.decisionhub.backend.dto.OptionRequestDto;
import com.decisionhub.backend.dto.OptionResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.UpdateDecisionRequest;
import com.decisionhub.backend.service.DecisionService;
import com.decisionhub.backend.service.OptionService;
import com.decisionhub.backend.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final DecisionService decisionService;
    private final OptionService optionService;

    public DecisionController(DecisionService decisionService, OptionService optionService) {
        this.decisionService = decisionService;
        this.optionService = optionService;
    }

    // =========================================================================
    // Decision CRUD Endpoints
    // =========================================================================

    @PostMapping
    public ResponseEntity<ApiResponseDto<DecisionResponseDto>> createDecision(
            @Valid @RequestBody CreateDecisionRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        DecisionResponseDto created = decisionService.createDecision(request, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Decision created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<DecisionResponseDto>>> getDecisions(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponseDto<DecisionResponseDto> decisions = decisionService.getPublicDecisions(pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Decisions retrieved successfully", decisions));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<DecisionResponseDto>>> getPublicDecisions(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponseDto<DecisionResponseDto> decisions = decisionService.getPublicDecisions(pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Public decisions retrieved successfully", decisions));
    }

    @GetMapping({"/my", "/me"})
    public ResponseEntity<ApiResponseDto<PagedResponseDto<DecisionResponseDto>>> getMyDecisions(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponseDto<DecisionResponseDto> decisions = decisionService.getMyDecisions(currentUserEmail, pageable);
        return ResponseEntity.ok(ApiResponseDto.success("User decisions retrieved successfully", decisions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DecisionResponseDto>> getDecisionById(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        DecisionResponseDto decision = decisionService.getDecisionById(id, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Decision retrieved successfully", decision));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponseDto<DecisionResponseDto>> getPublicDecisionById(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        DecisionResponseDto decision = decisionService.getDecisionById(id, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Decision retrieved successfully", decision));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DecisionResponseDto>> updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDecisionRequest request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        DecisionResponseDto updated = decisionService.updateDecision(id, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Decision updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteDecision(
            @PathVariable Long id,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        decisionService.deleteDecision(id, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Decision deleted successfully", null));
    }

    // =========================================================================
    // Option Management Endpoints
    // =========================================================================

    @PostMapping("/{decisionId}/options")
    public ResponseEntity<ApiResponseDto<List<OptionResponseDto>>> addOptions(
            @PathVariable Long decisionId,
            @Valid @RequestBody OptionBatchRequestDto request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<OptionResponseDto> createdOptions = optionService.addOptions(decisionId, request, currentUserEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Options added successfully", createdOptions));
    }

    @GetMapping("/{decisionId}/options")
    public ResponseEntity<ApiResponseDto<List<OptionResponseDto>>> getOptions(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        List<OptionResponseDto> options = optionService.getOptionsByDecisionId(decisionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Options retrieved successfully", options));
    }

    @PutMapping("/{decisionId}/options/{optionId}")
    public ResponseEntity<ApiResponseDto<OptionResponseDto>> updateOption(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            @Valid @RequestBody OptionRequestDto request,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        OptionResponseDto updated = optionService.updateOption(decisionId, optionId, request, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Option updated successfully", updated));
    }

    @DeleteMapping("/{decisionId}/options/{optionId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteOption(
            @PathVariable Long decisionId,
            @PathVariable Long optionId,
            Authentication authentication) {
        String currentUserEmail = authentication != null ? authentication.getName() : null;
        optionService.deleteOption(decisionId, optionId, currentUserEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Option deleted successfully", null));
    }
}

