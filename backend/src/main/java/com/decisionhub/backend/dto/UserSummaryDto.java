package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;

/**
 * Safe summary DTO for user information, omitting sensitive credentials.
 */
public record UserSummaryDto(
    Long id,
    String fullName,
    String email,
    UserRole role
) {
    public static UserSummaryDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDto(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole()
        );
    }
}

