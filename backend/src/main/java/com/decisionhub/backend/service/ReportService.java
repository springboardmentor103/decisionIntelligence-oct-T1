package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.CreateReportRequest;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.ReportResponseDto;
import com.decisionhub.backend.dto.ResolveReportRequest;
import com.decisionhub.backend.dto.UpdateReportStatusRequest;
import com.decisionhub.backend.entity.ReportStatus;
import com.decisionhub.backend.entity.ReportType;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for submitting, tracking, and resolving moderation reports.
 */
public interface ReportService {

    ReportResponseDto submitReport(CreateReportRequest request, String userEmail);

    PagedResponseDto<ReportResponseDto> getMyReports(String userEmail, Pageable pageable);

    PagedResponseDto<ReportResponseDto> getCommunityReports(Long communityId, ReportStatus status, ReportType reason, Pageable pageable, String userEmail);

    ReportResponseDto getReportById(Long reportId, String userEmail);

    ReportResponseDto updateReportStatus(Long reportId, UpdateReportStatusRequest request, String userEmail);

    ReportResponseDto resolveReport(Long reportId, ResolveReportRequest request, String userEmail);
}
