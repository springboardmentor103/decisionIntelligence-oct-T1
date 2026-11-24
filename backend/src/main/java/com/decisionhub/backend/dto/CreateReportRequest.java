package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for submitting a moderation report against content or a user.
 */
public record CreateReportRequest(
    @NotBlank(message = "Content type is required (e.g., DECISION, COMMENT, FEEDBACK)")
    String contentType,

    @NotNull(message = "Content ID is required")
    Long contentId,

    @NotNull(message = "Report reason type is required")
    ReportType reason,

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description
) {
}
