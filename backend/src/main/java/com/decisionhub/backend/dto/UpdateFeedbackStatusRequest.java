package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for changing the status of a feedback item.
 */
public record UpdateFeedbackStatusRequest(
    @NotNull(message = "Status is required")
    FeedbackStatus status,

    @Size(max = 500, message = "Status reason must not exceed 500 characters")
    String reason
) {
}
