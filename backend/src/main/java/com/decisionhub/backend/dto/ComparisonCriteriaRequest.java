package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ComparisonCriteriaRequest(

    @NotBlank(message = "Criteria name is required")
    @Size(max = 100, message = "Criteria name cannot exceed 100 characters")
    String name,

    @Size(max = 500, message = "Criteria description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Criteria weight is required")
    @PositiveOrZero(message = "Criteria weight cannot be negative")
    Double weight

) {}