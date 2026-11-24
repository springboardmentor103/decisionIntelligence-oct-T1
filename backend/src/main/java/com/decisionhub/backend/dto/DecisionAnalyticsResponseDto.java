package com.decisionhub.backend.dto;

import java.util.Map;

/**
 * Aggregated analytics for decisions by status, category, and visibility.
 */
public record DecisionAnalyticsResponseDto(
    long totalDecisions,
    long activeCount,
    long closedCount,
    long draftCount,
    long archivedCount,
    long publicCount,
    long privateCount,
    Map<String, Long> byCategory,
    Map<String, Long> byStatus,
    Map<String, Long> byVisibility
) {
}
