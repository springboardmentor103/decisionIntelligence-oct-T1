package com.decisionhub.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Audit log entity recording all moderation actions taken by moderators and administrators.
 */
@Entity
@Table(
    name = "moderation_history",
    indexes = {
        @Index(name = "idx_mod_history_moderator", columnList = "moderator_id"),
        @Index(name = "idx_mod_history_community", columnList = "community_id"),
        @Index(name = "idx_mod_history_target_user", columnList = "target_user_id"),
        @Index(name = "idx_mod_history_report", columnList = "report_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "moderator_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_mod_history_moderator")
    )
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "community_id",
        foreignKey = @ForeignKey(name = "fk_mod_history_community")
    )
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "target_user_id",
        foreignKey = @ForeignKey(name = "fk_mod_history_target_user")
    )
    private User targetUser;

    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "report_id",
        foreignKey = @ForeignKey(name = "fk_mod_history_report")
    )
    private Report report;
}
