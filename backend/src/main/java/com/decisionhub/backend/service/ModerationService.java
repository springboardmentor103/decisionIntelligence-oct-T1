package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.ContentModerationRequest;
import com.decisionhub.backend.dto.MemberRestrictionRequest;

/**
 * Service interface for direct content and community member moderation actions.
 */
public interface ModerationService {

    void moderateComment(Long commentId, ContentModerationRequest request, String userEmail);

    void moderateFeedback(Long feedbackId, ContentModerationRequest request, String userEmail);

    void restrictCommunityMember(Long communityId, Long userId, MemberRestrictionRequest request, String userEmail);

    void restoreCommunityMember(Long communityId, Long userId, String userEmail);
}
