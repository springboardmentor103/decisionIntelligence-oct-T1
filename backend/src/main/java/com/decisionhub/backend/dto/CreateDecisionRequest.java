package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.PollType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating a new decision.
 */
public record CreateDecisionRequest(
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    String category,

    DecisionVisibility visibility,

    DecisionStatus status,

    PollType pollType,

    Long communityId,

    LocalDateTime deadline,

    List<@Valid OptionRequestDto> options
) {
}

