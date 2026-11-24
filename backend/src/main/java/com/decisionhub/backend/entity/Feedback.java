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
 * Entity representing structured feedback on a decision and its options.
 */
@Entity
@Table(
    name = "feedbacks",
    indexes = {
        @Index(name = "idx_feedbacks_decision", columnList = "decision_id"),
        @Index(name = "idx_feedbacks_author", columnList = "author_id"),
        @Index(name = "idx_feedbacks_type", columnList = "feedback_type"),
        @Index(name = "idx_feedbacks_status", columnList = "status"),
        @Index(name = "idx_feedbacks_deleted", columnList = "is_deleted")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "decision_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedbacks_decision")
    )
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "author_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_feedbacks_author")
    )
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "option_id",
        foreignKey = @ForeignKey(name = "fk_feedbacks_option")
    )
    private Option option;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 30)
    private FeedbackType feedbackType;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private FeedbackStatus status = FeedbackStatus.SUBMITTED;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reviewed_by_id",
        foreignKey = @ForeignKey(name = "fk_feedbacks_reviewed_by")
    )
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "status_reason", length = 500)
    private String statusReason;
}
