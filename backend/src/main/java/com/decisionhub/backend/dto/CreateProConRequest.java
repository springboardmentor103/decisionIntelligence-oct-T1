package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.ProConType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for adding an advantage (Pro) or disadvantage (Con) to an option.
 */
public record CreateProConRequest(
    @NotNull(message = "Type (PRO or CON) is required")
    ProConType type,

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    Integer displayOrder
) {
}
