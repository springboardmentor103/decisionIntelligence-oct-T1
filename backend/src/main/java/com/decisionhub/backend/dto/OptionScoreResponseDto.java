package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.OptionScore;

import java.time.LocalDateTime;

/**
 * Response payload representing an individual score assigned to an option.
 */
public record OptionScoreResponseDto(
    Long id,
    Long optionId,
    Long criterionId,
    String criterionName,
    Double rawScore,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OptionScoreResponseDto fromEntity(OptionScore entity) {
        if (entity == null) {
            return null;
        }
        return new OptionScoreResponseDto(
            entity.getId(),
            entity.getOption() != null ? entity.getOption().getId() : null,
            entity.getCriterion() != null ? entity.getCriterion().getId() : null,
            entity.getCriterion() != null ? entity.getCriterion().getName() : null,
            entity.getRawScore(),
            entity.getNotes(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
