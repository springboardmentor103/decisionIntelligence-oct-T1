package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Community;

/**
 * Summary DTO for optional community affiliation.
 */
public record CommunitySummaryDto(
    Long id,
    String name,
    String description
) {
    public static CommunitySummaryDto fromEntity(Community community) {
        if (community == null) {
            return null;
        }
        return new CommunitySummaryDto(
            community.getId(),
            community.getName(),
            community.getDescription()
        );
    }
}

