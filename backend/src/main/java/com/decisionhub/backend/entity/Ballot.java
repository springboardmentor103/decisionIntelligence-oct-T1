package com.decisionhub.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a submitted ballot by a user for a decision/poll.
 * Enforces one ballot per user per decision at the database level.
 */
@Entity
@Table(
    name = "ballots",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_ballots_user_decision",
            columnNames = {"user_id", "decision_id"}
        ),
        @UniqueConstraint(
            name = "uk_ballots_id_decision",
            columnNames = {"id", "decision_id"}
        )
    },
    indexes = {
        @Index(name = "idx_ballots_user", columnList = "user_id"),
        @Index(name = "idx_ballots_decision", columnList = "decision_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ballot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ballots_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "decision_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ballots_decision")
    )
    private Decision decision;

    @OneToMany(mappedBy = "ballot", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BallotSelection> selections = new ArrayList<>();
}

