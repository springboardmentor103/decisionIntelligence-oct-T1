package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.ComparisonDtos;

import java.util.List;

public interface ComparisonService {

    ComparisonDtos.CriteriaResponse addCriteria(
            Long decisionId,
            ComparisonDtos.CreateCriteriaRequest request,
            String email
    );

    ComparisonDtos.ComparisonDetailsResponse getComparisonDetails(
            Long decisionId,
            String email
    );

    ComparisonDtos.ProsConsResponse addProsCons(
            Long optionId,
            ComparisonDtos.AddProsConsRequest request,
            String email
    );

    ComparisonDtos.ScoreResponse addScore(
            Long optionId,
            ComparisonDtos.AddScoreRequest request,
            String email
    );

    List<ComparisonDtos.RankedOptionResponse> getRankedOptions(
            Long decisionId,
            String email
    );
}