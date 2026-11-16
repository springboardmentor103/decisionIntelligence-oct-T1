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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an evaluation criterion defined for a decision.
 */
@Entity
@Table(
    name = "comparison_criteria",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_criteria_decision_name", columnNames = {"decision_id", "name"})
    },
    indexes = {
        @Index(name = "idx_criteria_decision", columnList = "decision_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonCriteria extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "decision_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_criteria_decision")
    )
    private Decision decision;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Double weight = 20.0;

    @Column(name = "min_score", nullable = false)
    @Builder.Default
    private Double minScore = 1.0;

    @Column(name = "max_score", nullable = false)
    @Builder.Default
    private Double maxScore = 10.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "polarity", nullable = false, length = 30)
    @Builder.Default
    private CriterionPolarity polarity = CriterionPolarity.HIGHER_IS_BETTER;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}
