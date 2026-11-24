
package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponse;
import com.decisionhub.backend.dto.OptionRequest;
import com.decisionhub.backend.dto.OptionResponse;
import com.decisionhub.backend.dto.ComparisonCriteriaResponse;

import java.util.List;

public interface DecisionService {

    DecisionResponse createDecision(
        CreateDecisionRequest request,
        String userEmail
    );

    List<DecisionResponse> getPublicDecisions();
    List<DecisionResponse> getCommunityPolls();

    List<DecisionResponse> getMyDecisions(String userEmail);

    DecisionResponse getDecisionById(
        Long decisionId,
        String userEmail
    );

    DecisionResponse updateDecision(
        Long decisionId,
        CreateDecisionRequest request,
        String userEmail
    );

    void deleteDecision(Long decisionId, String userEmail);

    OptionResponse addOption(
        Long decisionId,
        OptionRequest request,
        String userEmail
    );

    List<OptionResponse> getDecisionOptions(
        Long decisionId,
        String userEmail
    );
    List<ComparisonCriteriaResponse> getDecisionCriteria(
    Long decisionId,
    String userEmail
);
}
