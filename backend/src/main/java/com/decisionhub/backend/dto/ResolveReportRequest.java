package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for resolving a moderation report with an outcome action.
 */
public record ResolveReportRequest(
    @NotBlank(message = "Resolution action is required (e.g., DISMISS, REMOVE_CONTENT, RESTRICT_MEMBER)")
    String action,

    @Size(max = 1000, message = "Resolution notes must not exceed 1000 characters")
    String resolutionNotes
) {
}
