package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.Option;

/**
 * Summary DTO for the option associated with feedback.
 */
public record FeedbackOptionDto(
    Long id,
    String title
) {
    public static FeedbackOptionDto fromEntity(Option option) {
        if (option == null) {
            return null;
        }
        return new FeedbackOptionDto(
            option.getId(),
            option.getTitle()
        );
    }
}
