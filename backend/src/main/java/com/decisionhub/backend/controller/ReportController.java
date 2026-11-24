package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.CreateReportRequest;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.ReportResponseDto;
import com.decisionhub.backend.dto.ResolveReportRequest;
import com.decisionhub.backend.dto.UpdateReportStatusRequest;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;
import com.decisionhub.backend.service.ReportService;
import com.decisionhub.backend.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for submitting, retrieving, reviewing, and resolving content moderation reports.
 */
@RestController
@RequestMapping("/api/v1")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Submit a report against content (DECISION, COMMENT, or FEEDBACK).
     */
    @PostMapping("/reports")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> submitReport(
            @Valid @RequestBody CreateReportRequest request,
            Authentication authentication) {
        ReportResponseDto response = reportService.submitReport(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Report submitted successfully", response));
    }

    /**
     * Retrieve reports submitted by the authenticated user.
     */
    @GetMapping("/reports/my")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<ReportResponseDto>>> getMyReports(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponseDto<ReportResponseDto> response = reportService.getMyReports(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Reports retrieved successfully", response));
    }

    /**
     * Retrieve moderation reports for a specific community (moderators/owners/admins only).
     */
    @GetMapping("/communities/{communityId}/reports")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<ReportResponseDto>>> getCommunityReports(
            @PathVariable Long communityId,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportType reason,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponseDto<ReportResponseDto> response =
                reportService.getCommunityReports(communityId, status, reason, pageable, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Community reports retrieved successfully", response));
    }

    /**
     * Retrieve details of a specific report (reporter or authorized moderator).
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> getReportById(
            @PathVariable Long reportId,
            Authentication authentication) {
        ReportResponseDto response = reportService.getReportById(reportId, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Report retrieved successfully", response));
    }

    /**
     * Update review status of a report (moderator/admin only).
     */
    @PutMapping("/reports/{reportId}/status")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request,
            Authentication authentication) {
        ReportResponseDto response = reportService.updateReportStatus(reportId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Report status updated successfully", response));
    }

    /**
     * Resolve a report with a moderation action (moderator/admin only).
     */
    @PostMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> resolveReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveReportRequest request,
            Authentication authentication) {
        ReportResponseDto response = reportService.resolveReport(reportId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponseDto.success("Report resolved successfully", response));
    }
}
