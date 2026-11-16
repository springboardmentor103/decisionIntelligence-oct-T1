package com.decisionhub.backend.dto;

import java.util.List;

public class ComparisonDtos {

    public record CreateCriteriaRequest(
            String name,
            String description,
            Double weight
    ) {}

    public record AddProsConsRequest(
            String type,
            String content
    ) {}

    public record AddScoreRequest(
            Long criteriaId,
            Integer score
    ) {}

    public record CriteriaResponse(
            Long id,
            String name,
            String description,
            Double weight
    ) {}

    public record ProsConsResponse(
            Long id,
            String type,
            String content,
            String userName
    ) {}

    public record ScoreResponse(
            Long criteriaId,
            String criteriaName,
            Integer score,
            String userName
    ) {}

    public record OptionComparisonResponse(
            Long optionId,
            String title,
            String description,
            List<ProsConsResponse> prosCons,
            List<ScoreResponse> scores
    ) {}

    public record ComparisonDetailsResponse(
            Long decisionId,
            List<CriteriaResponse> criteria,
            List<OptionComparisonResponse> options
    ) {}

    public record RankedOptionResponse(
            Long optionId,
            String title,
            Double weightedScore,
            Integer totalScores
    ) {}
}