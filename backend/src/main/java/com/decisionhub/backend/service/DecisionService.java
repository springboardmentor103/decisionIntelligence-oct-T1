package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponseDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.UpdateDecisionRequest;
import org.springframework.data.domain.Pageable;

public interface DecisionService {

    /**
     * Create a new decision for the authenticated user.
     */
    DecisionResponseDto createDecision(CreateDecisionRequest request, String currentUserEmail);

    /**
     * Retrieve paginated public decisions.
     */
    PagedResponseDto<DecisionResponseDto> getPublicDecisions(Pageable pageable);

    /**
     * Retrieve paginated decisions created by the authenticated user.
     */
    PagedResponseDto<DecisionResponseDto> getMyDecisions(String currentUserEmail, Pageable pageable);

    /**
     * Retrieve a decision by ID, checking visibility and ownership authorization.
     */
    DecisionResponseDto getDecisionById(Long id, String currentUserEmail);

    /**
     * Update an existing decision. Only creator or administrator permitted.
     */
    DecisionResponseDto updateDecision(Long id, UpdateDecisionRequest request, String currentUserEmail);

    /**
     * Delete an existing decision. Only creator or administrator permitted.
     */
    void deleteDecision(Long id, String currentUserEmail);
}

