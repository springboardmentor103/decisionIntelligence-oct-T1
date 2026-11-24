package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CreateReportRequest;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.ReportResponseDto;
import com.decisionhub.backend.dto.ResolveReportRequest;
import com.decisionhub.backend.dto.UpdateReportStatusRequest;
import com.decisionhub.backend.entity.Comment;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.CommunityMember;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.Feedback;
import com.decisionhub.backend.entity.ModerationHistory;
import com.decisionhub.backend.entity.NotificationType;
import com.decisionhub.backend.entity.Report;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.CommentRepository;
import com.decisionhub.backend.repository.CommunityMemberRepository;
import com.decisionhub.backend.repository.CommunityRepository;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.FeedbackRepository;
import com.decisionhub.backend.repository.ModerationHistoryRepository;
import com.decisionhub.backend.repository.ReportRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.NotificationService;
import com.decisionhub.backend.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ReportService for managing content reporting and resolution.
 */
@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final DecisionRepository decisionRepository;
    private final CommentRepository commentRepository;
    private final FeedbackRepository feedbackRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final UserRepository userRepository;
    private final ModerationHistoryRepository moderationHistoryRepository;
    private final NotificationService notificationService;

    public ReportServiceImpl(
            ReportRepository reportRepository,
            DecisionRepository decisionRepository,
            CommentRepository commentRepository,
            FeedbackRepository feedbackRepository,
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository,
            UserRepository userRepository,
            ModerationHistoryRepository moderationHistoryRepository,
            NotificationService notificationService) {
        this.reportRepository = reportRepository;
        this.decisionRepository = decisionRepository;
        this.commentRepository = commentRepository;
        this.feedbackRepository = feedbackRepository;
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
        this.userRepository = userRepository;
        this.moderationHistoryRepository = moderationHistoryRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ReportResponseDto submitReport(CreateReportRequest request, String userEmail) {
        User reporter = getAuthenticatedUser(userEmail);
        String targetType = request.contentType().toUpperCase().trim();

        Decision decision;
        Comment comment = null;
        Feedback feedback = null;

        switch (targetType) {
            case "COMMENT" -> {
                comment = commentRepository.findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", request.contentId()));
                decision = comment.getDecision();
            }
            case "FEEDBACK" -> {
                feedback = feedbackRepository.findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", request.contentId()));
                decision = feedback.getDecision();
            }
            case "DECISION" -> {
                decision = decisionRepository.findById(request.contentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", request.contentId()));
            }
            default -> throw new BadRequestException("Invalid contentType: " + request.contentType() + ". Supported: DECISION, COMMENT, FEEDBACK");
        }

        // Prevent duplicate active reports from the same user for the same content
        boolean alreadyReported = reportRepository.existsByUserIdAndContentTypeAndContentIdAndStatusIn(
                reporter.getId(),
                targetType,
                request.contentId(),
                List.of(ReportStatus.PENDING, ReportStatus.UNDER_REVIEW)
        );

        if (alreadyReported) {
            throw new BadRequestException("You have already submitted an active report for this content.");
        }

        Report report = Report.builder()
                .user(reporter)
                .decision(decision)
                .contentType(targetType)
                .contentId(request.contentId())
                .comment(comment)
                .feedback(feedback)
                .reportType(request.reason())
                .description(request.description())
                .status(ReportStatus.PENDING)
                .build();

        Report saved = reportRepository.save(report);
        return ReportResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<ReportResponseDto> getMyReports(String userEmail, Pageable pageable) {
        User user = getAuthenticatedUser(userEmail);
        Page<Report> page = reportRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<ReportResponseDto> dtoList = page.getContent().stream()
                .map(ReportResponseDto::fromEntity)
                .toList();

        Page<ReportResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, page.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<ReportResponseDto> getCommunityReports(
            Long communityId,
            ReportStatus status,
            ReportType reason,
            Pageable pageable,
            String userEmail) {

        User user = getAuthenticatedUser(userEmail);
        Community community = findCommunityOrThrow(communityId);

        validateModeratorAccess(community, user);

        Page<Report> page;
        if (status != null && reason != null) {
            page = reportRepository.findByDecisionCommunityIdAndStatusAndReportTypeOrderByCreatedAtDesc(communityId, status, reason, pageable);
        } else if (status != null) {
            page = reportRepository.findByDecisionCommunityIdAndStatusOrderByCreatedAtDesc(communityId, status, pageable);
        } else if (reason != null) {
            page = reportRepository.findByDecisionCommunityIdAndReportTypeOrderByCreatedAtDesc(communityId, reason, pageable);
        } else {
            page = reportRepository.findByDecisionCommunityIdOrderByCreatedAtDesc(communityId, pageable);
        }

        List<ReportResponseDto> dtoList = page.getContent().stream()
                .map(ReportResponseDto::fromEntity)
                .toList();

        Page<ReportResponseDto> dtoPage = new PageImpl<>(dtoList, pageable, page.getTotalElements());
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto getReportById(Long reportId, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Report report = findReportOrThrow(reportId);

        boolean isReporter = report.getUser().getId().equals(user.getId());
        boolean isGlobalAdmin = user.getRole() == UserRole.ROLE_ADMIN;
        boolean isCommunityMod = isCommunityModerator(report.getDecision().getCommunity(), user);
        boolean isCommunityOwner = report.getDecision().getCommunity() != null
                && report.getDecision().getCommunity().getCreator().getId().equals(user.getId());

        if (!isReporter && !isGlobalAdmin && !isCommunityMod && !isCommunityOwner) {
            throw new ForbiddenException("You do not have permission to view this report.");
        }

        return ReportResponseDto.fromEntity(report);
    }

    @Override
    public ReportResponseDto updateReportStatus(Long reportId, UpdateReportStatusRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Report report = findReportOrThrow(reportId);

        validateModeratorAccess(report.getDecision().getCommunity(), user);

        report.setStatus(request.status());
        report.setReviewedBy(user);
        report.setReviewedAt(LocalDateTime.now());
        if (request.notes() != null && !request.notes().isBlank()) {
            report.setResolutionNotes(request.notes().trim());
        }

        Report saved = reportRepository.save(report);
        return ReportResponseDto.fromEntity(saved);
    }

    @Override
    public ReportResponseDto resolveReport(Long reportId, ResolveReportRequest request, String userEmail) {
        User user = getAuthenticatedUser(userEmail);
        Report report = findReportOrThrow(reportId);

        validateModeratorAccess(report.getDecision().getCommunity(), user);

        String action = request.action().toUpperCase().trim();
        User targetUser = null;

        switch (action) {
            case "DISMISS" -> {
                report.setStatus(ReportStatus.DISMISSED);
                report.setActionTaken("DISMISS");
            }
            case "REMOVE_CONTENT" -> {
                if (report.getComment() != null) {
                    Comment comment = report.getComment();
                    targetUser = comment.getUser();
                    comment.setDeleted(true);
                    comment.setContent("[This comment has been removed by moderator]");
                    commentRepository.save(comment);
                } else if (report.getFeedback() != null) {
                    Feedback feedback = report.getFeedback();
                    targetUser = feedback.getAuthor();
                    feedback.setDeleted(true);
                    feedback.setContent("[This feedback has been removed by moderator]");
                    feedbackRepository.save(feedback);
                } else if (report.getDecision() != null) {
                    Decision decision = report.getDecision();
                    targetUser = decision.getCreator();
                    decision.setStatus(DecisionStatus.CLOSED);
                    decisionRepository.save(decision);
                }
                report.setStatus(ReportStatus.RESOLVED);
                report.setActionTaken("REMOVE_CONTENT");
            }
            case "RESTRICT_MEMBER" -> {
                if (report.getDecision() != null && report.getDecision().getCommunity() != null) {
                    Community community = report.getDecision().getCommunity();
                    if (report.getComment() != null) {
                        targetUser = report.getComment().getUser();
                    } else if (report.getFeedback() != null) {
                        targetUser = report.getFeedback().getAuthor();
                    }

                    if (targetUser != null) {
                        if (community.getCreator().getId().equals(targetUser.getId())) {
                            throw new ForbiddenException("Cannot restrict the community owner.");
                        }

                        communityMemberRepository.findByCommunityIdAndUserId(community.getId(), targetUser.getId())
                                .ifPresent(member -> {
                                    member.setStatus(CommunityMembershipStatus.RESTRICTED);
                                    communityMemberRepository.save(member);
                                });
                    }
                }
                report.setStatus(ReportStatus.RESOLVED);
                report.setActionTaken("RESTRICT_MEMBER");
            }
            default -> throw new BadRequestException("Invalid resolution action: " + request.action() + ". Supported: DISMISS, REMOVE_CONTENT, RESTRICT_MEMBER");
        }

        report.setReviewedBy(user);
        report.setReviewedAt(LocalDateTime.now());
        if (request.resolutionNotes() != null && !request.resolutionNotes().isBlank()) {
            report.setResolutionNotes(request.resolutionNotes().trim());
        }

        Report saved = reportRepository.save(report);

        // Record in audit trail
        ModerationHistory history = ModerationHistory.builder()
                .moderator(user)
                .community(report.getDecision().getCommunity())
                .targetUser(targetUser)
                .contentType(report.getContentType())
                .contentId(report.getContentId())
                .action(action)
                .reason(request.resolutionNotes())
                .report(saved)
                .build();
        moderationHistoryRepository.save(history);

        // Notify reporter that report has been resolved
        notificationService.createNotification(
                report.getUser(),
                NotificationType.SYSTEM,
                "Your report #" + report.getId() + " regarding " + report.getContentType() + " has been resolved.",
                report.getId()
        );

        return ReportResponseDto.fromEntity(saved);
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

    private Report findReportOrThrow(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));
    }

    private Community findCommunityOrThrow(Long communityId) {
        return communityRepository.findById(communityId)
                .orElseThrow(() -> new ResourceNotFoundException("Community", "id", communityId));
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
            throw new ForbiddenException("You do not have permission to moderate reports for this community.");
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
