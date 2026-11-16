package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.PollType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Response DTO representing full decision details.
 */
public record DecisionResponseDto(
    Long id,
    String title,
    String description,
    String category,
    DecisionVisibility visibility,
    DecisionStatus status,
    PollType pollType,
    UserSummaryDto creator,
    CommunitySummaryDto community,
    LocalDateTime deadline,
    List<OptionResponseDto> options,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DecisionResponseDto fromEntity(Decision decision) {
        if (decision == null) {
            return null;
        }

        List<OptionResponseDto> optionDtos = List.of();
        if (decision.getOptions() != null) {
            optionDtos = decision.getOptions().stream()
                .sorted(Comparator.comparing(
                    opt -> opt.getDisplayOrder() != null ? opt.getDisplayOrder() : 0
                ))
                .map(OptionResponseDto::fromEntity)
                .toList();
        }

        return new DecisionResponseDto(
            decision.getId(),
            decision.getTitle(),
            decision.getDescription(),
            decision.getCategory(),
            decision.getVisibility(),
            decision.getStatus(),
            decision.getPollType(),
            UserSummaryDto.fromEntity(decision.getCreator()),
            CommunitySummaryDto.fromEntity(decision.getCommunity()),
            decision.getDeadline(),
            optionDtos,
            decision.getCreatedAt(),
            decision.getUpdatedAt()
        );
    }
}

