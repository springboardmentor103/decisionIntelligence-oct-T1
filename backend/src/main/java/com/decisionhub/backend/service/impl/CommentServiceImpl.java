package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CommentAuthorDto;
import com.decisionhub.backend.dto.CommentRequestDto;
import com.decisionhub.backend.dto.CommentResponseDto;
import com.decisionhub.backend.dto.DiscussionSummaryResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.entity.Comment;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.CommentRepository;
import com.decisionhub.backend.repository.CommunityMemberRepository;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.CommentService;
import com.decisionhub.backend.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CommentService for managing discussion threads, replies, and moderation.
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final NotificationService notificationService;

    public CommentServiceImpl(
            CommentRepository commentRepository,
            DecisionRepository decisionRepository,
            UserRepository userRepository,
            CommunityMemberRepository communityMemberRepository,
            NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.notificationService = notificationService;
    }

    @Override
    public CommentResponseDto createComment(Long decisionId, CommentRequestDto request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Decision decision = findDecisionOrThrow(decisionId);

        validateDecisionAccess(decision, user);

        String trimmedContent = validateAndTrimContent(request.content());

        Comment comment = Comment.builder()
                .decision(decision)
                .user(user)
                .content(trimmedContent)
                .parentComment(null)
                .deleted(false)
                .build();

        Comment saved = commentRepository.save(comment);

        // Notify decision creator if someone else commented
        if (!decision.getCreator().getId().equals(user.getId())) {
            notificationService.createNotification(
                    decision.getCreator(),
                    NotificationType.COMMENT_RECEIVED,
                    user.getFullName() + " commented on your decision: '" + decision.getTitle() + "'",
                    decision.getId()
            );
        }

        return CommentResponseDto.fromEntity(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<CommentResponseDto> getComments(Long decisionId, Pageable pageable, String userEmail) {
        Decision decision = findDecisionOrThrow(decisionId);

        User user = resolveOptionalUser(userEmail);
        validateDecisionAccess(decision, user);

        Page<Comment> commentPage = commentRepository.findByDecisionIdAndParentCommentIsNull(decisionId, pageable);

        List<Long> parentIds = commentPage.getContent().stream().map(Comment::getId).toList();
        Map<Long, Long> replyCounts = getReplyCountMap(parentIds);

        List<CommentResponseDto> dtoList = commentPage.getContent().stream()
                .map(c -> CommentResponseDto.fromEntity(c, replyCounts.getOrDefault(c.getId(), 0L)))
                .toList();

        Page<CommentResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, commentPage.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Comment comment = findCommentOrThrow(commentId);

        if (comment.isDeleted()) {
            throw new BadRequestException("Cannot edit a deleted comment.");
        }

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own comments.");
        }

        String trimmedContent = validateAndTrimContent(request.content());
        comment.setContent(trimmedContent);

        Comment saved = commentRepository.save(comment);
        long replyCount = commentRepository.countByParentCommentId(comment.getId());
        return CommentResponseDto.fromEntity(saved, replyCount);
    }

    @Override
    public void deleteComment(Long commentId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Comment comment = findCommentOrThrow(commentId);

        if (comment.isDeleted()) {
            return; // Already deleted
        }

        boolean isAuthor = comment.getUser().getId().equals(user.getId());
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;
        boolean isCommunityModerator = isCommunityModerator(comment.getDecision().getCommunity(), user);

        if (!isAuthor && !isGlobalAdmin && !isCommunityModerator) {
            throw new ForbiddenException("You do not have permission to delete or moderate this comment.");
        }

        // Soft delete: preserve reply thread integrity
        comment.setDeleted(true);
        comment.setContent("[This comment has been deleted]");
        commentRepository.save(comment);
    }

    @Override
    public CommentResponseDto createReply(Long commentId, CommentRequestDto request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Comment parentComment = findCommentOrThrow(commentId);

        Decision decision = parentComment.getDecision();
        validateDecisionAccess(decision, user);

        String trimmedContent = validateAndTrimContent(request.content());

        Comment reply = Comment.builder()
                .decision(decision)
                .user(user)
                .content(trimmedContent)
                .parentComment(parentComment)
                .deleted(false)
                .build();

        Comment saved = commentRepository.save(reply);

        // Notify parent comment author if someone else replied
        if (!parentComment.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(
                    parentComment.getUser(),
                    NotificationType.COMMENT_REPLY,
                    user.getFullName() + " replied to your comment on: '" + decision.getTitle() + "'",
                    decision.getId()
            );
        }

        return CommentResponseDto.fromEntity(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<CommentResponseDto> getReplies(Long commentId, Pageable pageable, String userEmail) {
        Comment parentComment = findCommentOrThrow(commentId);

        User user = resolveOptionalUser(userEmail);
        validateDecisionAccess(parentComment.getDecision(), user);

        Page<Comment> replyPage = commentRepository.findByParentCommentId(commentId, pageable);

        List<Long> replyIds = replyPage.getContent().stream().map(Comment::getId).toList();
        Map<Long, Long> nestedReplyCounts = getReplyCountMap(replyIds);

        List<CommentResponseDto> dtoList = replyPage.getContent().stream()
                .map(r -> CommentResponseDto.fromEntity(r, nestedReplyCounts.getOrDefault(r.getId(), 0L)))
                .toList();

        Page<CommentResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, replyPage.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscussionSummaryResponseDto getDiscussionSummary(Long decisionId, String userEmail) {
        Decision decision = findDecisionOrThrow(decisionId);

        User user = resolveOptionalUser(userEmail);
        validateDecisionAccess(decision, user);

        long totalComments = commentRepository.countByDecisionId(decisionId);
        long totalTopLevel = commentRepository.countByDecisionIdAndParentCommentIsNull(decisionId);
        long totalReplies = commentRepository.countByDecisionIdAndParentCommentIsNotNull(decisionId);
        LocalDateTime lastActivity = commentRepository.findLatestActivityByDecisionId(decisionId).orElse(null);

        return new DiscussionSummaryResponseDto(
                decisionId,
                totalComments,
                totalTopLevel,
                totalReplies,
                lastActivity
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

    private User resolveOptionalUser(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private Decision findDecisionOrThrow(Long decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
    }

    private String validateAndTrimContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Comment content cannot be blank.");
        }
        return content.trim();
    }

    private void validateDecisionAccess(Decision decision, User user) {
        if (decision.getVisibility() == DecisionVisibility.PUBLIC) {
            return;
        }

        // Private decision requires authentication
        if (user == null) {
            throw new UnauthorizedException("Authentication required to access private decision discussions.");
        }

        boolean isCreator = decision.getCreator().getId().equals(user.getId());
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;

        if (isCreator || isGlobalAdmin) {
            return;
        }

        // Check community membership if decision is linked to a community
        if (decision.getCommunity() != null) {
            Community community = decision.getCommunity();
            boolean isCommunityOwner = community.getCreator().getId().equals(user.getId());
            boolean isCommunityMember = communityMemberRepository
                    .existsByCommunityIdAndUserId(community.getId(), user.getId());

            if (isCommunityOwner || isCommunityMember) {
                return;
            }
        }

        throw new ForbiddenException("You do not have permission to access discussions for this private decision.");
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

    private Map<Long, Long> getReplyCountMap(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = commentRepository.countRepliesForParentIds(parentIds);
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : rows) {
            Long parentId = (Long) row[0];
            Long count = (Long) row[1];
            countMap.put(parentId, count);
        }
        return countMap;
    }
}
