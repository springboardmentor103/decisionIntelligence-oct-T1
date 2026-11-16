package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.CriterionPolarity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for defining a comparison criterion for a decision.
 */
public record CreateCriteriaRequest(
    @NotBlank(message = "Criterion name is required")
    @Size(max = 100, message = "Criterion name cannot exceed 100 characters")
    String name,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @Positive(message = "Weight must be greater than 0")
    Double weight,

    Double minScore,

    Double maxScore,

    CriterionPolarity polarity
) {
}
