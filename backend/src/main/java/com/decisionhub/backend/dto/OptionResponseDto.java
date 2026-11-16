package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Option;

import java.time.LocalDateTime;

/**
 * Response DTO for an individual option belonging to a decision.
 */
public record OptionResponseDto(
    Long id,
    Long decisionId,
    String title,
    String description,
    Integer displayOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static OptionResponseDto fromEntity(Option option) {
        if (option == null) {
            return null;
        }
        return new OptionResponseDto(
            option.getId(),
            option.getDecision() != null ? option.getDecision().getId() : null,
            option.getTitle(),
            option.getDescription(),
            option.getDisplayOrder(),
            option.getCreatedAt(),
            option.getUpdatedAt()
        );
    }
}

