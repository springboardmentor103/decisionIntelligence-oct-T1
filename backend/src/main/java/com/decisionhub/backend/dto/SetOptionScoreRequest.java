package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Single criterion score entry for an option.
 */
public record SetOptionScoreRequest(
    @NotNull(message = "Criterion ID is required")
    Long criterionId,

    @NotNull(message = "Raw score is required")
    Double rawScore,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes
) {
}
