package com.decisionhub.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a moderation report filed against content or users.
 */
@Entity
@Table(
    name = "reports",
    indexes = {
        @Index(name = "idx_reports_decision", columnList = "decision_id"),
        @Index(name = "idx_reports_user", columnList = "user_id"),
        @Index(name = "idx_reports_status", columnList = "status"),
        @Index(name = "idx_reports_content", columnList = "content_type, content_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_reports_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "decision_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_reports_decision")
    )
    private Decision decision;

    @Column(name = "content_type", length = 30)
    private String contentType;

    @Column(name = "content_id")
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "comment_id",
        foreignKey = @ForeignKey(name = "fk_reports_comment")
    )
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "feedback_id",
        foreignKey = @ForeignKey(name = "fk_reports_feedback")
    )
    private Feedback feedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reviewed_by_id",
        foreignKey = @ForeignKey(name = "fk_reports_reviewed_by")
    )
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "action_taken", length = 50)
    private String actionTaken;
}
