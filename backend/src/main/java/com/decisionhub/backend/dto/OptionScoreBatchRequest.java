package com.decisionhub.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Batch request payload for setting multiple criteria scores on an option.
 */
public record OptionScoreBatchRequest(
    @NotEmpty(message = "Scores list cannot be empty")
    List<@Valid SetOptionScoreRequest> scores
) {
}
