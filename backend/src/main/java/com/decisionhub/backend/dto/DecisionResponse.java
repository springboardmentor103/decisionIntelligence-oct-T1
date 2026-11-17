
package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.PollType;

import java.time.LocalDateTime;

public record DecisionResponse(

    Long id,
    String title,
    String description,
    String category,
    DecisionVisibility visibility,
    DecisionStatus status,
    PollType pollType,
    Long creatorId,
    String creatorName,
    Long communityId,
    LocalDateTime deadline,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {}
