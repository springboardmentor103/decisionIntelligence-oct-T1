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
 * Entity representing an evaluation score assigned to an option for a specific criterion.
 */
@Entity
@Table(
    name = "option_scores",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_option_scores_opt_crit", columnNames = {"option_id", "criterion_id"})
    },
    indexes = {
        @Index(name = "idx_opt_scores_option", columnList = "option_id"),
        @Index(name = "idx_opt_scores_criterion", columnList = "criterion_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionScore extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "option_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_opt_scores_option")
    )
    private Option option;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "criterion_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_opt_scores_criterion")
    )
    private ComparisonCriteria criterion;

    @Column(name = "raw_score", nullable = false)
    private Double rawScore;

    @Column(name = "notes", length = 500)
    private String notes;
}
