package com.decisionhub.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Complete evaluation matrix for a decision, containing criteria definitions,
 * option evaluations, qualitative comparisons, pros/cons, and deterministic rankings.
 */
public record ComparisonMatrixResponseDto(
    Long decisionId,
    String decisionTitle,
    List<CriteriaResponseDto> criteria,
    List<RankedOptionDto> rankedOptions,
    String winnerOptionTitle,
    LocalDateTime generatedAt
) {
}
