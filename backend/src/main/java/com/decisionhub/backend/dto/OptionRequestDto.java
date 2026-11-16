package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a decision option.
 */
public record OptionRequestDto(
    @NotBlank(message = "Option title is required")
    @Size(max = 200, message = "Option title must not exceed 200 characters")
    String title,

    String description,

    Integer displayOrder
) {
}

