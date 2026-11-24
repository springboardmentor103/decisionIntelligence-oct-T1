package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.AnalyticsOverviewResponseDto;
import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.CommunityAnalyticsResponseDto;
import com.decisionhub.backend.dto.DecisionAnalyticsResponseDto;
import com.decisionhub.backend.dto.DecisionDetailAnalyticsResponseDto;
import com.decisionhub.backend.dto.EngagementAnalyticsResponseDto;
import com.decisionhub.backend.dto.TrendAnalyticsResponseDto;
import com.decisionhub.backend.dto.VotingAnalyticsResponseDto;
import com.decisionhub.backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for platform, community, and decision analytics.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Retrieve high-level platform or user overview metrics.
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponseDto<AnalyticsOverviewResponseDto>> getOverview(
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        AnalyticsOverviewResponseDto response = analyticsService.getOverview(userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Analytics overview retrieved successfully", response));
    }

    /**
     * Retrieve aggregated decision metrics across categories, statuses, and visibilities.
     */
    @GetMapping("/decisions")
    public ResponseEntity<ApiResponseDto<DecisionAnalyticsResponseDto>> getDecisionAnalytics(
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        DecisionAnalyticsResponseDto response = analyticsService.getDecisionAnalytics(userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Decision analytics retrieved successfully", response));
    }

    /**
     * Retrieve voting participation metrics, poll types, and option breakdowns.
     */
    @GetMapping("/voting")
    public ResponseEntity<ApiResponseDto<VotingAnalyticsResponseDto>> getVotingAnalytics(
            @RequestParam(required = false) Long decisionId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        VotingAnalyticsResponseDto response = analyticsService.getVotingAnalytics(decisionId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Voting analytics retrieved successfully", response));
    }

    /**
     * Retrieve community metrics including member roles, statuses, and top communities.
     */
    @GetMapping("/communities")
    public ResponseEntity<ApiResponseDto<CommunityAnalyticsResponseDto>> getCommunityAnalytics(
            @RequestParam(required = false) Long communityId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        CommunityAnalyticsResponseDto response = analyticsService.getCommunityAnalytics(communityId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Community analytics retrieved successfully", response));
    }

    /**
     * Retrieve engagement statistics (comments, feedback, and most discussed decisions).
     */
    @GetMapping("/engagement")
    public ResponseEntity<ApiResponseDto<EngagementAnalyticsResponseDto>> getEngagementAnalytics(
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        EngagementAnalyticsResponseDto response = analyticsService.getEngagementAnalytics(userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Engagement analytics retrieved successfully", response));
    }

    /**
     * Retrieve time-series trend data for decisions, ballots, comments, and feedback.
     */
    @GetMapping("/trends")
    public ResponseEntity<ApiResponseDto<TrendAnalyticsResponseDto>> getTrendAnalytics(
            @RequestParam(defaultValue = "daily") String period,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        TrendAnalyticsResponseDto response = analyticsService.getTrendAnalytics(period, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Trend analytics retrieved successfully", response));
    }

    /**
     * Retrieve in-depth analytics for a single decision with option vote shares and average ratings.
     */
    @GetMapping("/decisions/{decisionId}")
    public ResponseEntity<ApiResponseDto<DecisionDetailAnalyticsResponseDto>> getDecisionDetailAnalytics(
            @PathVariable Long decisionId,
            Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : null;
        DecisionDetailAnalyticsResponseDto response = analyticsService.getDecisionDetailAnalytics(decisionId, userEmail);
        return ResponseEntity.ok(ApiResponseDto.success("Decision detail analytics retrieved successfully", response));
    }
}
