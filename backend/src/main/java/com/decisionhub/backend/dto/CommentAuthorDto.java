package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.User;

/**
 * Safe summary DTO for comment author without exposing sensitive fields.
 */
public record CommentAuthorDto(
    Long id,
    String fullName,
    String email
) {
    public static CommentAuthorDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new CommentAuthorDto(
            user.getId(),
            user.getFullName(),
            user.getEmail()
        );
    }
}
