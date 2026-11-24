package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.CommentRequestDto;
import com.decisionhub.backend.dto.CommentResponseDto;
import com.decisionhub.backend.dto.DiscussionSummaryResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.service.CommentService;
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
 * REST Controller for managing discussions, comments, and threaded replies.
 */
@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Create a top-level comment on a decision.
     */
    @PostMapping("/decisions/{decisionId}/comments")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> createComment(
            @PathVariable Long decisionId,
            @Valid @RequestBody CommentRequestDto request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        CommentResponseDto response = commentService.createComment(decisionId, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Comment created successfully", response));
    }

    /**
     * Retrieve paginated top-level comments for a decision.
     */
    @GetMapping("/decisions/{decisionId}/comments")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<CommentResponseDto>>> getComments(
            @PathVariable Long decisionId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String userEmail = authentication != null ? authentication.getName() : null;
        PagedResponseDto<CommentResponseDto> comments = commentService.getComments(decisionId, pageable, userEmail);

        return ResponseEntity.ok(ApiResponseDto.success("Comments retrieved successfully", comments));
    }

    /**
     * Update an existing comment (author only).
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        CommentResponseDto updated = commentService.updateComment(commentId, request, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Comment updated successfully", updated));
    }

    /**
     * Delete an existing comment (author, community moderator, or admin).
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Comment deleted successfully", null));
    }

    /**
     * Reply to an existing comment.
     */
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponseDto<CommentResponseDto>> createReply(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto request,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        CommentResponseDto reply = commentService.createReply(commentId, request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Reply created successfully", reply));
    }

    /**
     * Retrieve paginated replies for a parent comment.
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<CommentResponseDto>>> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        String userEmail = authentication != null ? authentication.getName() : null;
        PagedResponseDto<CommentResponseDto> replies = commentService.getReplies(commentId, pageable, userEmail);

        return ResponseEntity.ok(ApiResponseDto.success("Replies retrieved successfully", replies));
    }

    /**
     * Retrieve discussion summary statistics for a decision.
     */
    @GetMapping("/decisions/{decisionId}/discussions/summary")
    public ResponseEntity<ApiResponseDto<DiscussionSummaryResponseDto>> getDiscussionSummary(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        DiscussionSummaryResponseDto summary = commentService.getDiscussionSummary(decisionId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Discussion summary retrieved successfully", summary));
    }
}
