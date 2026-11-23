package com.decisionhub.backend.dto;

import java.time.LocalDateTime;

public class CommunityDtos {

    public record CreateCommunityRequest(
            String name,
            String description
    ) {}

    public record UpdateCommunityRequest(
            String name,
            String description
    ) {}

    public record CommunityResponse(
        Long id,
        String name,
        String description,
        Long creatorId,
        String creatorName,
        long memberCount,
        long decisionCount,
        LocalDateTime createdAt
) {}

    public record CommunityMemberResponse(
            Long userId,
            String fullName,
            String email,
            String role,
            String status,
            LocalDateTime joinedAt
    ) {}
}