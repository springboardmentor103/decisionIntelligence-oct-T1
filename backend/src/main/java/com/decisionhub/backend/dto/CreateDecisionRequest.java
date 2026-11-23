

package com.decisionhub.backend.dto;
import com.decisionhub.backend.dto.OptionRequest;

import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.PollType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateDecisionRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    String category,

    @NotNull(message = "Visibility is required")
    DecisionVisibility visibility,

    @NotNull(message = "Poll type is required")
    PollType pollType,

    LocalDateTime deadline,
    Boolean communityPoll,

    @Valid
    List<ComparisonCriteriaRequest> criteria,
    @Valid
    List<OptionRequest> options

) {}
