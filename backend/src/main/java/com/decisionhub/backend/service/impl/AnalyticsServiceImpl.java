package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.AnalyticsOverviewResponseDto;
import com.decisionhub.backend.dto.CommunityAnalyticsResponseDto;
import com.decisionhub.backend.dto.DecisionAnalyticsResponseDto;
import com.decisionhub.backend.dto.DecisionDetailAnalyticsResponseDto;
import com.decisionhub.backend.dto.EngagementAnalyticsResponseDto;
import com.decisionhub.backend.dto.OptionVoteAnalyticsDto;
import com.decisionhub.backend.dto.TopCommunityDto;
import com.decisionhub.backend.dto.TopDiscussedDecisionDto;
import com.decisionhub.backend.dto.TrendAnalyticsResponseDto;
import com.decisionhub.backend.dto.TrendPointDto;
import com.decisionhub.backend.dto.VotingAnalyticsResponseDto;
import com.decisionhub.backend.entity.BallotSelection;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.CommunityMember;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.PollType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.BallotRepository;
import com.decisionhub.backend.repository.BallotSelectionRepository;
import com.decisionhub.backend.repository.CommentRepository;
import com.decisionhub.backend.repository.CommunityMemberRepository;
import com.decisionhub.backend.repository.CommunityRepository;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.FeedbackRepository;
import com.decisionhub.backend.repository.OptionRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.AnalyticsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation of AnalyticsService providing efficient read-only metrics and aggregations.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final BallotRepository ballotRepository;
    private final BallotSelectionRepository ballotSelectionRepository;
    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final OptionRepository optionRepository;

    public AnalyticsServiceImpl(
            DecisionRepository decisionRepository,
            UserRepository userRepository,
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository,
            BallotRepository ballotRepository,
            BallotSelectionRepository ballotSelectionRepository,
            CommentRepository commentRepository,
            FeedbackRepository feedbackRepository,
            OptionRepository optionRepository) {
        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.ballotRepository = ballotRepository;
        this.ballotSelectionRepository = ballotSelectionRepository;
        this.commentRepository = commentRepository;
        this.feedbackRepository = feedbackRepository;
        this.optionRepository = optionRepository;
    }

    @Override
    public AnalyticsOverviewResponseDto getOverview(String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;

        long totalDecisions = decisionRepository.count();
        long activeDecisions = decisionRepository.countByStatus(DecisionStatus.ACTIVE);
        long closedDecisions = decisionRepository.countByStatus(DecisionStatus.CLOSED);
        long totalCommunities = communityRepository.count();
        Long totalUsers = isGlobalAdmin ? userRepository.count() : null;
        long totalBallots = ballotRepository.count();
        long totalComments = commentRepository.count();
        long totalFeedback = feedbackRepository.countByDeletedFalse();

        return new AnalyticsOverviewResponseDto(
                totalDecisions,
                activeDecisions,
                closedDecisions,
                totalCommunities,
                totalUsers,
                totalBallots,
                totalComments,
                totalFeedback,
                isGlobalAdmin
        );
    }

    @Override
    public DecisionAnalyticsResponseDto getDecisionAnalytics(String userEmail) {
        getAuthenticatedUser(userEmail);

        long totalDecisions = decisionRepository.count();
        long activeCount = decisionRepository.countByStatus(DecisionStatus.ACTIVE);
        long closedCount = decisionRepository.countByStatus(DecisionStatus.CLOSED);
        long draftCount = decisionRepository.countByStatus(DecisionStatus.DRAFT);
        long archivedCount = decisionRepository.countByStatus(DecisionStatus.ARCHIVED);
        long publicCount = decisionRepository.countByVisibility(DecisionVisibility.PUBLIC);
        long privateCount = decisionRepository.countByVisibility(DecisionVisibility.PRIVATE);

        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Object[] row : decisionRepository.countGroupByCategory()) {
            if (row[0] != null) {
                byCategory.put(row[0].toString(), (Long) row[1]);
            }
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : decisionRepository.countGroupByStatus()) {
            if (row[0] != null) {
                byStatus.put(row[0].toString(), (Long) row[1]);
            }
        }

        Map<String, Long> byVisibility = new LinkedHashMap<>();
        for (Object[] row : decisionRepository.countGroupByVisibility()) {
            if (row[0] != null) {
                byVisibility.put(row[0].toString(), (Long) row[1]);
            }
        }

        return new DecisionAnalyticsResponseDto(
                totalDecisions,
                activeCount,
                closedCount,
                draftCount,
                archivedCount,
                publicCount,
                privateCount,
                byCategory,
                byStatus,
                byVisibility
        );
    }

    @Override
    public VotingAnalyticsResponseDto getVotingAnalytics(Long decisionId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);

        long totalBallots = ballotRepository.count();
        long totalSelections = ballotSelectionRepository.count();
        Long activeDecisionsWithVoting = ballotRepository.countDistinctDecisionsWithBallots();

        Map<String, Long> byPollType = new LinkedHashMap<>();
        for (Object[] row : decisionRepository.countGroupByPollType()) {
            if (row[0] != null) {
                byPollType.put(row[0].toString(), (Long) row[1]);
            }
        }

        List<OptionVoteAnalyticsDto> optionDistributions = new ArrayList<>();
        if (decisionId != null) {
            Decision decision = findDecisionOrThrow(decisionId);
            validateDecisionAccess(decision, user);

            List<BallotSelection> selections = ballotSelectionRepository.findByDecisionId(decisionId);
            List<Option> options = optionRepository.findByDecisionIdOrderByDisplayOrderAsc(decisionId);
            long totalDecisionSelections = selections.size();

            for (Option option : options) {
                List<BallotSelection> optSelections = selections.stream()
                        .filter(s -> s.getOption().getId().equals(option.getId()))
                        .toList();

                long voteCount = optSelections.size();
                double percentage = totalDecisionSelections == 0 ? 0.0 : Math.round((voteCount * 100.0 / totalDecisionSelections) * 10.0) / 10.0;

                Double averageRating = null;
                if (decision.getPollType() == PollType.RATING) {
                    averageRating = optSelections.stream()
                            .map(BallotSelection::getRating)
                            .filter(r -> r != null)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0);
                    averageRating = Math.round(averageRating * 10.0) / 10.0;
                }

                optionDistributions.add(new OptionVoteAnalyticsDto(
                        option.getId(),
                        option.getTitle(),
                        voteCount,
                        percentage,
                        averageRating
                ));
            }
        }

        return new VotingAnalyticsResponseDto(
                totalBallots,
                totalSelections,
                activeDecisionsWithVoting != null ? activeDecisionsWithVoting : 0L,
                byPollType,
                optionDistributions
        );
    }

    @Override
    public CommunityAnalyticsResponseDto getCommunityAnalytics(Long communityId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);

        long totalCommunities = communityRepository.count();
        long totalMemberships = communityMemberRepository.count();

        Map<String, Long> byRole = new LinkedHashMap<>();
        for (Object[] row : communityMemberRepository.countGroupByRole()) {
            if (row[0] != null) {
                byRole.put(row[0].toString(), (Long) row[1]);
            }
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : communityMemberRepository.countGroupByStatus()) {
            if (row[0] != null) {
                byStatus.put(row[0].toString(), (Long) row[1]);
            }
        }

        List<TopCommunityDto> topCommunities = new ArrayList<>();
        List<Object[]> rows = communityMemberRepository.findTopCommunitiesByActiveMembers(PageRequest.of(0, 5));
        for (Object[] row : rows) {
            topCommunities.add(new TopCommunityDto(
                    (Long) row[0],
                    (String) row[1],
                    (Long) row[2]
            ));
        }

        return new CommunityAnalyticsResponseDto(
                totalCommunities,
                totalMemberships,
                byRole,
                byStatus,
                topCommunities
        );
    }

    @Override
    public EngagementAnalyticsResponseDto getEngagementAnalytics(String userEmail) {
        getAuthenticatedUser(userEmail);

        long totalComments = commentRepository.count();
        long totalReplies = commentRepository.countByParentCommentIsNotNull();
        long totalFeedback = feedbackRepository.countByDeletedFalse();

        Map<String, Long> feedbackByType = new LinkedHashMap<>();
        for (Object[] row : feedbackRepository.countAllGroupByFeedbackType()) {
            if (row[0] != null) {
                feedbackByType.put(row[0].toString(), (Long) row[1]);
            }
        }

        Map<String, Long> feedbackByStatus = new LinkedHashMap<>();
        for (Object[] row : feedbackRepository.countAllGroupByStatus()) {
            if (row[0] != null) {
                feedbackByStatus.put(row[0].toString(), (Long) row[1]);
            }
        }

        List<TopDiscussedDecisionDto> topDiscussed = new ArrayList<>();
        List<Object[]> rows = commentRepository.findTopDiscussedDecisions(PageRequest.of(0, 5));
        for (Object[] row : rows) {
            topDiscussed.add(new TopDiscussedDecisionDto(
                    (Long) row[0],
                    (String) row[1],
                    (Long) row[2]
            ));
        }

        return new EngagementAnalyticsResponseDto(
                totalComments,
                totalReplies,
                totalFeedback,
                feedbackByType,
                feedbackByStatus,
                topDiscussed
        );
    }

    @Override
    public TrendAnalyticsResponseDto getTrendAnalytics(String period, String userEmail) {
        getAuthenticatedUser(userEmail);

        Map<String, long[]> rawPoints = new TreeMap<>(); // date -> [decisions, ballots, comments, feedback]

        // 1. Decisions created
        for (Object[] row : decisionRepository.countCreatedDaily()) {
            String date = row[0].toString();
            rawPoints.computeIfAbsent(date, k -> new long[4])[0] = (Long) row[1];
        }

        // 2. Ballots submitted
        for (Object[] row : ballotRepository.countBallotsDaily()) {
            String date = row[0].toString();
            rawPoints.computeIfAbsent(date, k -> new long[4])[1] = (Long) row[1];
        }

        // 3. Comments posted
        for (Object[] row : commentRepository.countCommentsDaily()) {
            String date = row[0].toString();
            rawPoints.computeIfAbsent(date, k -> new long[4])[2] = (Long) row[1];
        }

        // 4. Feedback submitted
        for (Object[] row : feedbackRepository.countFeedbackDaily()) {
            String date = row[0].toString();
            rawPoints.computeIfAbsent(date, k -> new long[4])[3] = (Long) row[1];
        }

        List<TrendPointDto> points = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : rawPoints.entrySet()) {
            long[] counts = entry.getValue();
            points.add(new TrendPointDto(
                    entry.getKey(),
                    counts[0],
                    counts[1],
                    counts[2],
                    counts[3]
            ));
        }

        return new TrendAnalyticsResponseDto(
                period != null ? period : "daily",
                points
        );
    }

    @Override
    public DecisionDetailAnalyticsResponseDto getDecisionDetailAnalytics(Long decisionId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Decision decision = findDecisionOrThrow(decisionId);

        validateDecisionAccess(decision, user);

        long totalBallots = ballotRepository.countByDecisionId(decisionId);
        List<BallotSelection> selections = ballotSelectionRepository.findByDecisionId(decisionId);
        long totalSelections = selections.size();
        long totalComments = commentRepository.countByDecisionId(decisionId);
        long totalFeedback = feedbackRepository.countByDecisionIdAndDeletedFalse(decisionId);

        List<Option> options = optionRepository.findByDecisionIdOrderByDisplayOrderAsc(decisionId);
        List<OptionVoteAnalyticsDto> optionResults = new ArrayList<>();

        for (Option option : options) {
            List<BallotSelection> optSelections = selections.stream()
                    .filter(s -> s.getOption().getId().equals(option.getId()))
                    .toList();

            long voteCount = optSelections.size();
            double percentage = totalSelections == 0 ? 0.0 : Math.round((voteCount * 100.0 / totalSelections) * 10.0) / 10.0;

            Double averageRating = null;
            if (decision.getPollType() == PollType.RATING) {
                averageRating = optSelections.stream()
                        .map(BallotSelection::getRating)
                        .filter(r -> r != null)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);
                averageRating = Math.round(averageRating * 10.0) / 10.0;
            }

            optionResults.add(new OptionVoteAnalyticsDto(
                    option.getId(),
                    option.getTitle(),
                    voteCount,
                    percentage,
                    averageRating
            ));
        }

        return new DecisionDetailAnalyticsResponseDto(
                decision.getId(),
                decision.getTitle(),
                decision.getCategory(),
                decision.getVisibility().name(),
                decision.getStatus().name(),
                decision.getPollType() != null ? decision.getPollType().name() : null,
                totalBallots,
                totalSelections,
                totalComments,
                totalFeedback,
                optionResults
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

    private Decision findDecisionOrThrow(Long decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));
    }

    private void validateDecisionAccess(Decision decision, User user) {
        if (decision.getVisibility() == DecisionVisibility.PUBLIC) {
            return;
        }

        boolean isCreator = decision.getCreator().getId().equals(user.getId());
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;

        if (isCreator || isGlobalAdmin) {
            return;
        }

        if (decision.getCommunity() != null) {
            Community community = decision.getCommunity();
            boolean isCommunityOwner = community.getCreator().getId().equals(user.getId());
            boolean isMember = communityMemberRepository.existsByCommunityIdAndUserId(community.getId(), user.getId());

            if (isCommunityOwner || isMember) {
                return;
            }
        }

        throw new ForbiddenException("You do not have permission to view analytics for this private decision.");
    }
}
