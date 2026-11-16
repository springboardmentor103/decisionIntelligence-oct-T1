package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.CriterionPolarity;

/**
 * Detailed evaluation breakdown for an option under a specific criterion.
 */
public record CriterionScoreDetailDto(
    Long criterionId,
    String criterionName,
    Double weight,
    Double normalizedWeight,
    CriterionPolarity polarity,
    Double rawScore,
    Double normalizedScore,
    Double weightedContribution,
    boolean isFallback,
    String notes
) {
}
