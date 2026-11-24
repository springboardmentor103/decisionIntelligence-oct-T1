package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.User;

/**
 * Safe summary DTO for feedback author or reviewer without exposing sensitive fields.
 */
public record FeedbackAuthorDto(
    Long id,
    String fullName,
    String email
) {
    public static FeedbackAuthorDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new FeedbackAuthorDto(
            user.getId(),
            user.getFullName(),
            user.getEmail()
        );
    }
}
