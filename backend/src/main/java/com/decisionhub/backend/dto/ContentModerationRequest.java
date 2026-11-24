package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for direct content moderation (hiding, removing, or restoring content).
 */
public record ContentModerationRequest(
    @NotBlank(message = "Action is required (e.g., DELETE, HIDE)")
    String action,

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    String reason
) {
}
