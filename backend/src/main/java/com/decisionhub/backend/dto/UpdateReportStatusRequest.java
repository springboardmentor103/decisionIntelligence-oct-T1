package com.decisionhub.backend.dto;

import com.decisionhub.backend.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating the review status of a report.
 */
public record UpdateReportStatusRequest(
    @NotNull(message = "Report status is required")
    ReportStatus status,

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes
) {
}
