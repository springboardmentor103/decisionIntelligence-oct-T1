package com.decisionhub.backend.dto;

import jakarta.validation.constraints.Size;

/**
 * Request payload for restricting a community member.
 */
public record MemberRestrictionRequest(
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    String reason
) {
}
