package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a comment.
 */
public record CommentRequestDto(
    @NotBlank(message = "Comment content cannot be blank")
    @Size(max = 5000, message = "Comment content must not exceed 5000 characters")
    String content
) {
}
