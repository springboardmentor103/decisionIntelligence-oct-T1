package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.ComparisonCriteria;
import com.decisionhub.backend.entity.CriterionPolarity;

import java.time.LocalDateTime;

/**
 * Response payload representing a comparison criterion.
 */
public record CriteriaResponseDto(
    Long id,
    Long decisionId,
    String name,
    String description,
    Double weight,
    Double minScore,
    Double maxScore,
    CriterionPolarity polarity,
    Boolean isDefault,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CriteriaResponseDto fromEntity(ComparisonCriteria criteria) {
        if (criteria == null) {
            return null;
        }
        return new CriteriaResponseDto(
            criteria.getId(),
            criteria.getDecision() != null ? criteria.getDecision().getId() : null,
            criteria.getName(),
            criteria.getDescription(),
            criteria.getWeight(),
            criteria.getMinScore(),
            criteria.getMaxScore(),
            criteria.getPolarity(),
            criteria.getIsDefault(),
            criteria.getCreatedAt(),
            criteria.getUpdatedAt()
        );
    }
}
