package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.ProConType;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing Pro or Con entry.
 */
public record UpdateProConRequest(
    ProConType type,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    Integer displayOrder
) {
}
