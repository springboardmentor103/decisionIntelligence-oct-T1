package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for submitting new feedback on a decision.
 */
public record CreateFeedbackRequest(
    @NotNull(message = "Feedback type is required")
    FeedbackType feedbackType,

    @NotBlank(message = "Feedback content cannot be blank")
    @Size(max = 5000, message = "Feedback content must not exceed 5000 characters")
    String content,

    Long optionId
) {
}
