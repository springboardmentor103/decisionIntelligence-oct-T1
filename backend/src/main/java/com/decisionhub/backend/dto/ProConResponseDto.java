package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.OptionProsCons;
import com.decisionhub.backend.entity.ProConType;

import java.time.LocalDateTime;

/**
 * Response payload representing an advantage (Pro) or disadvantage (Con) of an option.
 */
public record ProConResponseDto(
    Long id,
    Long optionId,
    ProConType type,
    String description,
    Integer displayOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProConResponseDto fromEntity(OptionProsCons entity) {
        if (entity == null) {
            return null;
        }
        return new ProConResponseDto(
            entity.getId(),
            entity.getOption() != null ? entity.getOption().getId() : null,
            entity.getType(),
            entity.getDescription(),
            entity.getDisplayOrder(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
