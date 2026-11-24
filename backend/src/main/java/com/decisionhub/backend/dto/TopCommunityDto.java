package com.decisionhub.backend.dto;

/**
 * Summary DTO for active communities.
 */
public record TopCommunityDto(
    Long communityId,
    String communityName,
    long memberCount
) {
}
