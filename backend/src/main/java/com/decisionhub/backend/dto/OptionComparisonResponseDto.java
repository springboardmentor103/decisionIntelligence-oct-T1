package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.OptionComparison;

import java.time.LocalDateTime;

/**
 * Response payload representing qualitative comparison details for an option.
 */
public record OptionComparisonResponseDto(
    Long id,
    Long optionId,
    String featureValues,
    String costDetails,
    String benefitDetails,
    String riskDetails,
    String timeDetails,
    String convenienceDetails,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OptionComparisonResponseDto fromEntity(OptionComparison comparison) {
        if (comparison == null) {
            return null;
        }
        return new OptionComparisonResponseDto(
            comparison.getId(),
            comparison.getOption() != null ? comparison.getOption().getId() : null,
            comparison.getFeatureValues(),
            comparison.getCostDetails(),
            comparison.getBenefitDetails(),
            comparison.getRiskDetails(),
            comparison.getTimeDetails(),
            comparison.getConvenienceDetails(),
            comparison.getCreatedAt(),
            comparison.getUpdatedAt()
        );
    }
}
