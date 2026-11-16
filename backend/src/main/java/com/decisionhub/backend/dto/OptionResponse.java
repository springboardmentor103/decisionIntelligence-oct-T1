
package com.decisionhub.backend.dto;

import java.time.LocalDateTime;

public record OptionResponse(
    Long id,
    Long decisionId,
    String title,
    String description,
    Integer displayOrder,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
