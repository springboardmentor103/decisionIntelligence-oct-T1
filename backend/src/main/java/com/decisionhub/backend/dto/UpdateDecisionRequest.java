package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.PollType;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for updating an existing decision.
 */
public record UpdateDecisionRequest(
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    String description,

    @Size(max = 50, message = "Category must not exceed 50 characters")
    String category,

    DecisionVisibility visibility,

    DecisionStatus status,

    PollType pollType,

    Long communityId,

    LocalDateTime deadline
) {
}

