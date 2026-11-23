package com.decisionhub.backend.dto;

public record ComparisonCriteriaResponse(
    Long id,
    Long decisionId,
    String name,
    String description,
    Double weight
) {}