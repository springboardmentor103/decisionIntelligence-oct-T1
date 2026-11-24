package com.decisionhub.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * Community analytics covering membership distribution and top communities.
 */
public record CommunityAnalyticsResponseDto(
    long totalCommunities,
    long totalMemberships,
    Map<String, Long> byRole,
    Map<String, Long> byStatus,
    List<TopCommunityDto> mostActiveCommunities
) {
}
