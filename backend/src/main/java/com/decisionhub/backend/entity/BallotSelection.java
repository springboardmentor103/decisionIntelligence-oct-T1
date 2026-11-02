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

/**
 * Entity representing an individual option chosen within a ballot.
 * Supports single-choice (1 selection), multiple-choice (N selections),
 * and rating-based voting via the rating attribute.
 */
@Entity
@Table(
    name = "ballot_selections",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ballot_selections_ballot_option",
            columnNames = {"ballot_id", "option_id"}
        )
    },
    indexes = {
        @Index(name = "idx_ballot_selections_ballot", columnList = "ballot_id"),
        @Index(name = "idx_ballot_selections_option", columnList = "option_id"),
        @Index(name = "idx_ballot_selections_decision", columnList = "decision_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BallotSelection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "decision_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ballot_selections_decision")
    )
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "ballot_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ballot_selections_ballot")
    )
    private Ballot ballot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "option_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ballot_selections_option")
    )
    private Option option;

    @Column(name = "rating")
    private Integer rating;
}
