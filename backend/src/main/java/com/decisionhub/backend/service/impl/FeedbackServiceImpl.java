package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CreateFeedbackRequest;
import com.decisionhub.backend.dto.FeedbackResponseDto;
import com.decisionhub.backend.dto.FeedbackSummaryResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.UpdateFeedbackRequest;
import com.decisionhub.backend.dto.UpdateFeedbackStatusRequest;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Feedback;
import com.decisionhub.backend.entity.FeedbackStatus;
import com.decisionhub.backend.entity.FeedbackType;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.CommunityMemberRepository;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.FeedbackRepository;
import com.decisionhub.backend.repository.OptionRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.FeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of FeedbackService for managing structured feedback, moderation, and statuses.
 */
@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final DecisionRepository decisionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;
    private final CommunityMemberRepository communityMemberRepository;

    private static final Map<FeedbackStatus, Set<FeedbackStatus>> VALID_TRANSITIONS = Map.of(
        FeedbackStatus.SUBMITTED, Set.of(FeedbackStatus.UNDER_REVIEW, FeedbackStatus.ACCEPTED, FeedbackStatus.REJECTED, FeedbackStatus.RESOLVED),
        FeedbackStatus.UNDER_REVIEW, Set.of(FeedbackStatus.ACCEPTED, FeedbackStatus.REJECTED, FeedbackStatus.RESOLVED, FeedbackStatus.SUBMITTED),
        FeedbackStatus.ACCEPTED, Set.of(FeedbackStatus.RESOLVED, FeedbackStatus.UNDER_REVIEW, FeedbackStatus.REJECTED),
        FeedbackStatus.REJECTED, Set.of(FeedbackStatus.UNDER_REVIEW, FeedbackStatus.SUBMITTED),
        FeedbackStatus.RESOLVED, Set.of(FeedbackStatus.UNDER_REVIEW)
    );

    public FeedbackServiceImpl(
            FeedbackRepository feedbackRepository,
            DecisionRepository decisionRepository,
            OptionRepository optionRepository,
            UserRepository userRepository,
            CommunityMemberRepository communityMemberRepository) {
        this.feedbackRepository = feedbackRepository;
        this.decisionRepository = decisionRepository;
        this.optionRepository = optionRepository;
        this.userRepository = userRepository;
        this.communityMemberRepository = communityMemberRepository;
    }

    @Override
    public FeedbackResponseDto submitFeedback(Long decisionId, CreateFeedbackRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Decision decision = findDecisionOrThrow(decisionId);

        validateDecisionAccess(decision, user);

        if (request.feedbackType() == null) {
            throw new BadRequestException("Feedback type is required.");
        }

        String trimmedContent = validateAndTrimContent(request.content());

        Option option = null;
        if (request.optionId() != null) {
            option = findOptionOrThrow(request.optionId());
            if (!option.getDecision().getId().equals(decision.getId())) {
                throw new BadRequestException("The specified option does not belong to this decision.");
            }
        }

        Feedback feedback = Feedback.builder()
                .decision(decision)
                .author(user)
                .option(option)
                .feedbackType(request.feedbackType())
                .content(trimmedContent)
                .status(FeedbackStatus.SUBMITTED)
                .deleted(false)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return FeedbackResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<FeedbackResponseDto> getFeedbackList(Long decisionId, FeedbackType feedbackType, Pageable pageable, String userEmail) {
        Decision decision = findDecisionOrThrow(decisionId);

        User user = resolveOptionalUser(userEmail);
        validateDecisionAccess(decision, user);

        Page<Feedback> feedbackPage;
        if (feedbackType != null) {
            feedbackPage = feedbackRepository.findByDecisionIdAndFeedbackTypeAndDeletedFalse(decisionId, feedbackType, pageable);
        } else {
            feedbackPage = feedbackRepository.findByDecisionIdAndDeletedFalse(decisionId, pageable);
        }

        List<FeedbackResponseDto> dtoList = feedbackPage.getContent().stream()
                .map(FeedbackResponseDto::fromEntity)
                .toList();

        Page<FeedbackResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, feedbackPage.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackResponseDto getFeedbackById(Long feedbackId, String userEmail) {
        Feedback feedback = findFeedbackOrThrow(feedbackId);

        if (feedback.isDeleted()) {
            throw new ResourceNotFoundException("Feedback", "id", feedbackId);
        }

        User user = resolveOptionalUser(userEmail);
        validateDecisionAccess(feedback.getDecision(), user);

        return FeedbackResponseDto.fromEntity(feedback);
    }

    @Override
    public FeedbackResponseDto updateFeedback(Long feedbackId, UpdateFeedbackRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Feedback feedback = findFeedbackOrThrow(feedbackId);

        if (feedback.isDeleted()) {
            throw new BadRequestException("Cannot edit deleted feedback.");
        }

        if (!feedback.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own feedback.");
        }

        String trimmedContent = validateAndTrimContent(request.content());
        feedback.setContent(trimmedContent);

        if (request.feedbackType() != null) {
            feedback.setFeedbackType(request.feedbackType());
        }

        if (request.optionId() != null) {
            Option option = findOptionOrThrow(request.optionId());
            if (!option.getDecision().getId().equals(feedback.getDecision().getId())) {
                throw new BadRequestException("The specified option does not belong to this decision.");
            }
            feedback.setOption(option);
        }

        Feedback saved = feedbackRepository.save(feedback);
        return FeedbackResponseDto.fromEntity(saved);
    }

    @Override
    public void deleteFeedback(Long feedbackId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Feedback feedback = findFeedbackOrThrow(feedbackId);

        if (feedback.isDeleted()) {
            return; // Idempotent
        }

        boolean isAuthor = feedback.getAuthor().getId().equals(user.getId());
        boolean isDecisionOwner = feedback.getDecision().getCreator().getId().equals(user.getId());
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;
        boolean isCommunityModerator = isCommunityModerator(feedback.getDecision().getCommunity(), user);

        if (!isAuthor && !isDecisionOwner && !isGlobalAdmin && !isCommunityModerator) {
            throw new ForbiddenException("You do not have permission to delete or moderate this feedback.");
        }

        // Soft delete
        feedback.setDeleted(true);
        feedback.setContent("[This feedback has been deleted]");
        feedbackRepository.save(feedback);
    }

    @Override
    public FeedbackResponseDto updateFeedbackStatus(Long feedbackId, UpdateFeedbackStatusRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Feedback feedback = findFeedbackOrThrow(feedbackId);

        if (feedback.isDeleted()) {
            throw new BadRequestException("Cannot change status of deleted feedback.");
        }

        boolean isDecisionOwner = feedback.getDecision().getCreator().getId().equals(user.getId());
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;
        boolean isCommunityModerator = isCommunityModerator(feedback.getDecision().getCommunity(), user);

        if (!isDecisionOwner && !isGlobalAdmin && !isCommunityModerator) {
            throw new ForbiddenException("Only decision owners, community moderators, or administrators can update feedback status.");
        }

        FeedbackStatus currentStatus = feedback.getStatus();
        FeedbackStatus targetStatus = request.status();

        if (targetStatus == null) {
            throw new BadRequestException("Target feedback status is required.");
        }

        if (currentStatus != targetStatus) {
            validateStatusTransition(currentStatus, targetStatus);
        }

        feedback.setStatus(targetStatus);
        feedback.setReviewedBy(user);
        feedback.setReviewedAt(LocalDateTime.now());
        if (request.reason() != null && !request.reason().isBlank()) {
            feedback.setStatusReason(request.reason().trim());
        }

        Feedback saved = feedbackRepository.save(feedback);
        return FeedbackResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackSummaryResponseDto getFeedbackSummary(Long decisionId, String userEmail) {
        Decision decision = findDecisionOrThrow(decisionId);

        User user = resolveOptionalUser(userEmail);
        validateDecisionAccess(decision, user);

        long totalFeedback = feedbackRepository.countByDecisionIdAndDeletedFalse(decisionId);

        Map<FeedbackType, Long> countsByType = new EnumMap<>(FeedbackType.class);
        for (FeedbackType type : FeedbackType.values()) {
            countsByType.put(type, 0L);
        }
        for (Object[] row : feedbackRepository.countByDecisionIdGroupByFeedbackType(decisionId)) {
            countsByType.put((FeedbackType) row[0], (Long) row[1]);
        }

        Map<FeedbackStatus, Long> countsByStatus = new EnumMap<>(FeedbackStatus.class);
        for (FeedbackStatus status : FeedbackStatus.values()) {
            countsByStatus.put(status, 0L);
        }
        for (Object[] row : feedbackRepository.countByDecisionIdGroupByStatus(decisionId)) {
            countsByStatus.put((FeedbackStatus) row[0], (Long) row[1]);
        }

        LocalDateTime lastActivity = feedbackRepository.findLatestActivityByDecisionId(decisionId).orElse(null);

        return new FeedbackSummaryResponseDto(
                decisionId,
                totalFeedback,
                countsByType,
                countsByStatus,
                lastActivity
        );
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void validateStatusTransition(FeedbackStatus current, FeedbackStatus target) {
        Set<FeedbackStatus> allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new BadRequestException("Invalid feedback status transition from " + current + " to " + target + ".");
        }
    }

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

    private Option findOptionOrThrow(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option", "id", optionId));
    }

    private Feedback findFeedbackOrThrow(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId));
    }

    private String validateAndTrimContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Feedback content cannot be blank.");
        }
        return content.trim();
    }

    private void validateDecisionAccess(Decision decision, User user) {
        if (decision.getVisibility() == DecisionVisibility.PUBLIC) {
            return;
        }

        // Private decision requires authentication
        if (user == null) {
            throw new UnauthorizedException("Authentication required to access private decision feedback.");
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

        throw new ForbiddenException("You do not have permission to access feedback for this private decision.");
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
