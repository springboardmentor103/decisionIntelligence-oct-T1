package com.decisionhub.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for batch adding options to a decision.
 */
public record OptionBatchRequestDto(
    @NotEmpty(message = "Options list must not be empty")
    List<@Valid OptionRequestDto> options
) {
}
