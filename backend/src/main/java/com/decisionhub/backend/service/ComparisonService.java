package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.ComparisonMatrixResponseDto;
import com.decisionhub.backend.dto.CreateCriteriaRequest;
import com.decisionhub.backend.dto.CreateProConRequest;
import com.decisionhub.backend.dto.CriteriaResponseDto;
import com.decisionhub.backend.dto.OptionComparisonRequest;
import com.decisionhub.backend.dto.OptionComparisonResponseDto;
import com.decisionhub.backend.dto.OptionScoreBatchRequest;
import com.decisionhub.backend.dto.OptionScoreResponseDto;
import com.decisionhub.backend.dto.ProConResponseDto;
import com.decisionhub.backend.dto.RankedOptionDto;
import com.decisionhub.backend.dto.UpdateCriteriaRequest;
import com.decisionhub.backend.dto.UpdateProConRequest;

import java.util.List;

public interface ComparisonService {

    // Criteria Management
    CriteriaResponseDto createCriteria(Long decisionId, CreateCriteriaRequest request, String currentUserEmail);

    List<CriteriaResponseDto> createDefaultCriteria(Long decisionId, String currentUserEmail);

    List<CriteriaResponseDto> getCriteriaByDecisionId(Long decisionId, String currentUserEmail);

    CriteriaResponseDto updateCriteria(Long decisionId, Long criteriaId, UpdateCriteriaRequest request, String currentUserEmail);

    void deleteCriteria(Long decisionId, Long criteriaId, String currentUserEmail);

    // Qualitative Comparison Data
    OptionComparisonResponseDto upsertOptionComparison(Long decisionId, Long optionId, OptionComparisonRequest request, String currentUserEmail);

    OptionComparisonResponseDto getOptionComparison(Long decisionId, Long optionId, String currentUserEmail);

    // Option Numerical Scores
    List<OptionScoreResponseDto> setOptionScores(Long decisionId, Long optionId, OptionScoreBatchRequest request, String currentUserEmail);

    List<OptionScoreResponseDto> getOptionScores(Long decisionId, Long optionId, String currentUserEmail);

    // Pros and Cons
    ProConResponseDto addProCon(Long decisionId, Long optionId, CreateProConRequest request, String currentUserEmail);

    List<ProConResponseDto> getProsAndCons(Long decisionId, Long optionId, String currentUserEmail);

    ProConResponseDto updateProCon(Long decisionId, Long optionId, Long proConId, UpdateProConRequest request, String currentUserEmail);

    void deleteProCon(Long decisionId, Long optionId, Long proConId, String currentUserEmail);

    // Evaluation Matrix & Rankings
    ComparisonMatrixResponseDto getComparisonMatrix(Long decisionId, String currentUserEmail);

    List<RankedOptionDto> getRankedOptions(Long decisionId, String currentUserEmail);
}
