package com.decisionhub.backend.dto;

import java.util.List;

/**
 * Time-series trend analytics across platform activities.
 */
public record TrendAnalyticsResponseDto(
    String period,
    List<TrendPointDto> points
) {
}
