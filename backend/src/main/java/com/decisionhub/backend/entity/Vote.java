package com.decisionhub.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a vote cast by a user for a specific option under a decision.
 */
@Entity
@Table(
    name = "votes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_votes_user_decision", columnNames = {"user_id", "decision_id"})
    },
    indexes = {
        @Index(name = "idx_votes_decision", columnList = "decision_id"),
        @Index(name = "idx_votes_user", columnList = "user_id"),
        @Index(name = "idx_votes_option", columnList = "option_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_votes_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "decision_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_votes_decision")
    )
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "option_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_votes_option")
    )
    private Option option;

    @CreationTimestamp
    @Column(name = "vote_date", nullable = false, updatable = false)
    private LocalDateTime voteDate;
}

