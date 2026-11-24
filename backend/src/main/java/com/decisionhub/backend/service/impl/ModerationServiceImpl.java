package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.ContentModerationRequest;
import com.decisionhub.backend.dto.MemberRestrictionRequest;
import com.decisionhub.backend.entity.Comment;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.CommunityMember;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import com.decisionhub.backend.entity.Feedback;
import com.decisionhub.backend.entity.ModerationHistory;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.CommentRepository;
import com.decisionhub.backend.repository.CommunityMemberRepository;
import com.decisionhub.backend.repository.CommunityRepository;
import com.decisionhub.backend.repository.FeedbackRepository;
import com.decisionhub.backend.repository.ModerationHistoryRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.ModerationService;
import com.decisionhub.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ModerationService for direct content actions and member restrictions.
 */
@Service
@Transactional
public class ModerationServiceImpl implements ModerationService {

    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;
    private final ModerationHistoryRepository moderationHistoryRepository;
    private final NotificationService notificationService;

    public ModerationServiceImpl(
            CommentRepository commentRepository,
            FeedbackRepository feedbackRepository,
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository,
            UserRepository userRepository,
            ModerationHistoryRepository moderationHistoryRepository,
            NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.feedbackRepository = feedbackRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.userRepository = userRepository;
        this.moderationHistoryRepository = moderationHistoryRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void moderateComment(Long commentId, ContentModerationRequest request, String userEmail) {
        User moderator = getAuthenticatedUser(userEmail);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        validateModeratorAccess(comment.getDecision().getCommunity(), moderator);

        comment.setDeleted(true);
        comment.setContent("[This comment has been removed by a moderator]");
        commentRepository.save(comment);

        // Record audit history
        ModerationHistory history = ModerationHistory.builder()
                .moderator(moderator)
                .community(comment.getDecision().getCommunity())
                .targetUser(comment.getUser())
                .contentType("COMMENT")
                .contentId(commentId)
                .action("MODERATE_COMMENT")
                .reason(request.reason())
                .build();
        moderationHistoryRepository.save(history);
    }

    @Override
    public void moderateFeedback(Long feedbackId, ContentModerationRequest request, String userEmail) {
        User moderator = getAuthenticatedUser(userEmail);
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId));

        validateModeratorAccess(feedback.getDecision().getCommunity(), moderator);

        feedback.setDeleted(true);
        feedback.setContent("[This feedback has been removed by a moderator]");
        feedbackRepository.save(feedback);

        // Record audit history
        ModerationHistory history = ModerationHistory.builder()
                .moderator(moderator)
                .community(feedback.getDecision().getCommunity())
                .targetUser(feedback.getAuthor())
                .contentType("FEEDBACK")
                .contentId(feedbackId)
                .action("MODERATE_FEEDBACK")
                .reason(request.reason())
                .build();
        moderationHistoryRepository.save(history);
    }

    @Override
    public void restrictCommunityMember(Long communityId, Long userId, MemberRestrictionRequest request, String userEmail) {
        User moderator = getAuthenticatedUser(userEmail);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community", "id", communityId));

        validateModeratorAccess(community, moderator);

        if (community.getCreator().getId().equals(userId)) {
            throw new ForbiddenException("Cannot restrict the community owner.");
        }

        CommunityMember member = communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CommunityMember", "userId", userId));

        member.setStatus(CommunityMembershipStatus.RESTRICTED);
        communityMemberRepository.save(member);

        // Record audit history
        ModerationHistory history = ModerationHistory.builder()
                .moderator(moderator)
                .community(community)
                .targetUser(member.getUser())
                .contentType("MEMBER")
                .contentId(userId)
                .action("RESTRICT_MEMBER")
                .reason(request != null ? request.reason() : "Community moderation policy violation")
                .build();
        moderationHistoryRepository.save(history);

        // Notify restricted user
        notificationService.createNotification(
                member.getUser(),
                NotificationType.MEMBER_REMOVED,
                "Your membership in '" + community.getName() + "' has been restricted by a moderator.",
                communityId
        );
    }

    @Override
    public void restoreCommunityMember(Long communityId, Long userId, String userEmail) {
        User moderator = getAuthenticatedUser(userEmail);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community", "id", communityId));

        validateModeratorAccess(community, moderator);

        CommunityMember member = communityMemberRepository.findByCommunityIdAndUserId(communityId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("CommunityMember", "userId", userId));

        member.setStatus(CommunityMembershipStatus.ACTIVE);
        communityMemberRepository.save(member);

        // Record audit history
        ModerationHistory history = ModerationHistory.builder()
                .moderator(moderator)
                .community(community)
                .targetUser(member.getUser())
                .contentType("MEMBER")
                .contentId(userId)
                .action("RESTORE_MEMBER")
                .reason("Member restriction lifted by moderator")
                .build();
        moderationHistoryRepository.save(history);

        // Notify restored user
        notificationService.createNotification(
                member.getUser(),
                NotificationType.MEMBERSHIP_APPROVED,
                "Your membership in '" + community.getName() + "' has been restored to active status.",
                communityId
        );
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private User getAuthenticatedUser(String email) {
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Authentication required.");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void validateModeratorAccess(Community community, User user) {
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;
        if (isGlobalAdmin) {
            return;
        }

        if (community == null) {
            throw new ForbiddenException("Only global administrators can moderate non-community content.");
        }

        boolean isOwner = community.getCreator().getId().equals(user.getId());
        boolean isModerator = isCommunityModerator(community, user);

        if (!isOwner && !isModerator) {
            throw new ForbiddenException("You do not have permission to moderate this community.");
        }
    }

    private boolean isCommunityModerator(Community community, User user) {
        if (community == null || user == null) {
            return false;
        }

        if (community.getCreator().getId().equals(user.getId())) {
            return true;
        }

        return communityMemberRepository.findByCommunityIdAndUserId(community.getId(), user.getId())
                .filter(m -> m.getStatus() == CommunityMembershipStatus.ACTIVE)
                .map(m -> m.getRole() == CommunityMembershipRole.MODERATOR || m.getRole() == CommunityMembershipRole.ADMIN)
                .orElse(false);
    }
}
