package com.decisionhub.backend.dto;

/**
 * Request payload for upserting qualitative comparison information for an option.
 */
public record OptionComparisonRequest(
    String featureValues,
    String costDetails,
    String benefitDetails,
    String riskDetails,
    String timeDetails,
    String convenienceDetails
) {
}
