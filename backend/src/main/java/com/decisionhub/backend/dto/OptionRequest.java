
package com.decisionhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OptionRequest(

    @NotBlank(message = "Option title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    String title,

    String description,

    Integer displayOrder

) {}
