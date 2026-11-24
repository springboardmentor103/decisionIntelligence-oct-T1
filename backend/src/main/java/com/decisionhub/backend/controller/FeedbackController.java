package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.CreateFeedbackRequest;
import com.decisionhub.backend.dto.FeedbackResponseDto;
import com.decisionhub.backend.dto.FeedbackSummaryResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.UpdateFeedbackRequest;
import com.decisionhub.backend.dto.UpdateFeedbackStatusRequest;
import com.decisionhub.backend.entity.FeedbackType;
import com.decisionhub.backend.service.FeedbackService;
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

/**
 * REST Controller for managing decision feedback, reviews, and status workflows.
 */
@RestController
@RequestMapping("/api/v1")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * Submit structured feedback on a decision.
     */
    @PostMapping("/decisions/{decisionId}/feedback")
    public ResponseEntity<ApiResponseDto<FeedbackResponseDto>> submitFeedback(
            @PathVariable Long decisionId,
            @Valid @RequestBody CreateFeedbackRequest request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        FeedbackResponseDto response = feedbackService.submitFeedback(decisionId, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Feedback submitted successfully", response));
    }

    /**
     * Retrieve paginated feedback for a decision with optional type filtering.
     */
    @GetMapping("/decisions/{decisionId}/feedback")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<FeedbackResponseDto>>> getFeedbackList(
            @PathVariable Long decisionId,
            @RequestParam(required = false) FeedbackType feedbackType,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String userEmail = authentication != null ? authentication.getName() : null;
        PagedResponseDto<FeedbackResponseDto> list = feedbackService.getFeedbackList(decisionId, feedbackType, pageable, userEmail);

        return ResponseEntity.ok(ApiResponseDto.success("Feedback retrieved successfully", list));
    }

    /**
     * Retrieve an individual feedback item by ID.
     */
    @GetMapping("/feedback/{feedbackId}")
    public ResponseEntity<ApiResponseDto<FeedbackResponseDto>> getFeedbackById(
            @PathVariable Long feedbackId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        FeedbackResponseDto feedback = feedbackService.getFeedbackById(feedbackId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Feedback details retrieved successfully", feedback));
    }

    /**
     * Update an existing feedback item (author only).
     */
    @PutMapping("/feedback/{feedbackId}")
    public ResponseEntity<ApiResponseDto<FeedbackResponseDto>> updateFeedback(
            @PathVariable Long feedbackId,
            @Valid @RequestBody UpdateFeedbackRequest request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        FeedbackResponseDto updated = feedbackService.updateFeedback(feedbackId, request, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Feedback updated successfully", updated));
    }

    /**
     * Delete an existing feedback item (author, decision owner, community moderator, or admin).
     */
    @DeleteMapping("/feedback/{feedbackId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteFeedback(
            @PathVariable Long feedbackId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        feedbackService.deleteFeedback(feedbackId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Feedback deleted successfully", null));
    }

    /**
     * Change feedback status (decision owner, community moderator, or admin only).
     */
    @PutMapping("/feedback/{feedbackId}/status")
    public ResponseEntity<ApiResponseDto<FeedbackResponseDto>> updateFeedbackStatus(
            @PathVariable Long feedbackId,
            @Valid @RequestBody UpdateFeedbackStatusRequest request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        FeedbackResponseDto updated = feedbackService.updateFeedbackStatus(feedbackId, request, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Feedback status updated successfully", updated));
    }

    /**
     * Retrieve aggregated feedback summary for a decision.
     */
    @GetMapping("/decisions/{decisionId}/feedback/summary")
    public ResponseEntity<ApiResponseDto<FeedbackSummaryResponseDto>> getFeedbackSummary(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        FeedbackSummaryResponseDto summary = feedbackService.getFeedbackSummary(decisionId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Feedback summary retrieved successfully", summary));
    }
}
