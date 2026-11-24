package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.ContentModerationRequest;
import com.decisionhub.backend.dto.MemberRestrictionRequest;
import com.decisionhub.backend.service.ModerationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for direct content moderation and community member restriction actions.
 */
@RestController
@RequestMapping("/api/v1")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /**
     * Moderate (hide/remove) an inappropriate comment.
     */
    @PutMapping("/comments/{commentId}/moderation")
    public ResponseEntity<ApiResponseDto<Void>> moderateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ContentModerationRequest request,
            Authentication authentication) {
        moderationService.moderateComment(commentId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Comment moderated successfully", null));
    }

    /**
     * Moderate (hide/remove) inappropriate feedback.
     */
    @PutMapping("/feedback/{feedbackId}/moderation")
    public ResponseEntity<ApiResponseDto<Void>> moderateFeedback(
            @PathVariable Long feedbackId,
            @Valid @RequestBody ContentModerationRequest request,
            Authentication authentication) {
        moderationService.moderateFeedback(feedbackId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Feedback moderated successfully", null));
    }

    /**
     * Restrict a community member from participating in a community.
     */
    @PostMapping("/communities/{communityId}/members/{userId}/restrict")
    public ResponseEntity<ApiResponseDto<Void>> restrictMember(
            @PathVariable Long communityId,
            @PathVariable Long userId,
            @RequestBody(required = false) MemberRestrictionRequest request,
            Authentication authentication) {
        moderationService.restrictCommunityMember(communityId, userId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Community member restricted successfully", null));
    }

    /**
     * Restore a restricted community member to active status.
     */
    @PostMapping("/communities/{communityId}/members/{userId}/restore")
    public ResponseEntity<ApiResponseDto<Void>> restoreMember(
            @PathVariable Long communityId,
            @PathVariable Long userId,
            Authentication authentication) {
        moderationService.restoreCommunityMember(communityId, userId, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Community member restored successfully", null));
    }
}
