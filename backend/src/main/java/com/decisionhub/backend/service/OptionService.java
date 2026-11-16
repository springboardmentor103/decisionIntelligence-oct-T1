package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.OptionBatchRequestDto;
import com.decisionhub.backend.dto.OptionRequestDto;
import com.decisionhub.backend.dto.OptionResponseDto;

import java.util.List;

public interface OptionService {

    /**
     * Add multiple options to a decision via DTO wrapper.
     */
    List<OptionResponseDto> addOptions(Long decisionId, OptionBatchRequestDto request, String currentUserEmail);

    /**
     * Add multiple options to a decision via direct list.
     */
    List<OptionResponseDto> addOptions(Long decisionId, List<OptionRequestDto> options, String currentUserEmail);

    /**
     * Retrieve all options for a decision, respecting private decision access restrictions.
     */
    List<OptionResponseDto> getOptionsByDecisionId(Long decisionId, String currentUserEmail);

    /**
     * Update an option within a decision, validating ownership and option-decision association.
     */
    OptionResponseDto updateOption(Long decisionId, Long optionId, OptionRequestDto request, String currentUserEmail);

    /**
     * Delete an option within a decision, validating ownership and option-decision association.
     */
    void deleteOption(Long decisionId, Long optionId, String currentUserEmail);
}
