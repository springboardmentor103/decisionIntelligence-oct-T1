package com.decisionhub.backend.dto;

import java.util.List;

/**
 * Representation of an evaluated option with comprehensive rankings, scores, and qualitative data.
 */
public record RankedOptionDto(
    int rank,
    Long optionId,
    String title,
    String description,
    Integer displayOrder,
    Double totalWeightedScore,
    Double scorePercentage,
    List<CriterionScoreDetailDto> criterionScores,
    OptionComparisonResponseDto comparisonData,
    List<ProConResponseDto> pros,
    List<ProConResponseDto> cons
) {
}
