package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Report;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;

import java.time.LocalDateTime;

/**
 * Response DTO representing detailed report information.
 */
public record ReportResponseDto(
    Long id,
    Long reporterId,
    String reporterName,
    Long decisionId,
    Long communityId,
    String contentType,
    Long contentId,
    ReportType reason,
    String description,
    ReportStatus status,
    Long reviewedById,
    String reviewedByName,
    LocalDateTime reviewedAt,
    String resolutionNotes,
    String actionTaken,
    LocalDateTime createdAt
) {
    public static ReportResponseDto fromEntity(Report report) {
        if (report == null) {
            return null;
        }

        Long communityId = (report.getDecision() != null && report.getDecision().getCommunity() != null)
                ? report.getDecision().getCommunity().getId()
                : null;

        return new ReportResponseDto(
            report.getId(),
            report.getUser() != null ? report.getUser().getId() : null,
            report.getUser() != null ? report.getUser().getFullName() : null,
            report.getDecision() != null ? report.getDecision().getId() : null,
            communityId,
            report.getContentType(),
            report.getContentId(),
            report.getReportType(),
            report.getDescription(),
            report.getStatus(),
            report.getReviewedBy() != null ? report.getReviewedBy().getId() : null,
            report.getReviewedBy() != null ? report.getReviewedBy().getFullName() : null,
            report.getReviewedAt(),
            report.getResolutionNotes(),
            report.getActionTaken(),
            report.getCreatedAt()
        );
    }
}
