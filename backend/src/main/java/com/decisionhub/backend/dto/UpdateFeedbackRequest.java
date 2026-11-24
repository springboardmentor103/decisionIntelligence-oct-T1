package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating existing feedback.
 */
public record UpdateFeedbackRequest(
    FeedbackType feedbackType,

    @NotBlank(message = "Feedback content cannot be blank")
    @Size(max = 5000, message = "Feedback content must not exceed 5000 characters")
    String content,

    Long optionId
) {
}
