package com.decisionhub.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing qualitative comparison attributes and feature specifications for an option.
 */
@Entity
@Table(
    name = "option_comparisons",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_option_comparisons_option", columnNames = {"option_id"})
    },
    indexes = {
        @Index(name = "idx_opt_comparisons_option", columnList = "option_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionComparison extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "option_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_opt_comparisons_option")
    )
    private Option option;

    @Column(name = "feature_values", columnDefinition = "TEXT")
    private String featureValues;

    @Column(name = "cost_details", columnDefinition = "TEXT")
    private String costDetails;

    @Column(name = "benefit_details", columnDefinition = "TEXT")
    private String benefitDetails;

    @Column(name = "risk_details", columnDefinition = "TEXT")
    private String riskDetails;

    @Column(name = "time_details", columnDefinition = "TEXT")
    private String timeDetails;

    @Column(name = "convenience_details", columnDefinition = "TEXT")
    private String convenienceDetails;
}
