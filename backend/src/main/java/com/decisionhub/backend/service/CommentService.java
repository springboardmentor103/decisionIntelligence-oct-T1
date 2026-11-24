package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.CommentRequestDto;
import com.decisionhub.backend.dto.CommentResponseDto;
import com.decisionhub.backend.dto.DiscussionSummaryResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing decision discussions, comments, and threaded replies.
 */
public interface CommentService {

    /**
     * Create a top-level comment on a decision.
     */
    CommentResponseDto createComment(Long decisionId, CommentRequestDto request, String userEmail);

    /**
     * Retrieve paginated top-level comments for a decision.
     */
    PagedResponseDto<CommentResponseDto> getComments(Long decisionId, Pageable pageable, String userEmail);

    /**
     * Update an existing comment (author only).
     */
    CommentResponseDto updateComment(Long commentId, CommentRequestDto request, String userEmail);

    /**
     * Delete a comment (author, community moderator, or admin).
     */
    void deleteComment(Long commentId, String userEmail);

    /**
     * Create a threaded reply to an existing comment.
     */
    CommentResponseDto createReply(Long commentId, CommentRequestDto request, String userEmail);

    /**
     * Retrieve paginated replies for a parent comment.
     */
    PagedResponseDto<CommentResponseDto> getReplies(Long commentId, Pageable pageable, String userEmail);

    /**
     * Retrieve discussion summary statistics for a decision.
     */
    DiscussionSummaryResponseDto getDiscussionSummary(Long decisionId, String userEmail);
}
