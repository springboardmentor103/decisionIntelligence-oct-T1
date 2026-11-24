package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.AnalyticsOverviewResponseDto;
import com.decisionhub.backend.dto.CommunityAnalyticsResponseDto;
import com.decisionhub.backend.dto.DecisionAnalyticsResponseDto;
import com.decisionhub.backend.dto.DecisionDetailAnalyticsResponseDto;
import com.decisionhub.backend.dto.EngagementAnalyticsResponseDto;
import com.decisionhub.backend.dto.TrendAnalyticsResponseDto;
import com.decisionhub.backend.dto.VotingAnalyticsResponseDto;

/**
 * Service interface for platform, community, and decision analytics.
 */
public interface AnalyticsService {

    AnalyticsOverviewResponseDto getOverview(String userEmail);

    DecisionAnalyticsResponseDto getDecisionAnalytics(String userEmail);

    VotingAnalyticsResponseDto getVotingAnalytics(Long decisionId, String userEmail);

    CommunityAnalyticsResponseDto getCommunityAnalytics(Long communityId, String userEmail);

    EngagementAnalyticsResponseDto getEngagementAnalytics(String userEmail);

    TrendAnalyticsResponseDto getTrendAnalytics(String period, String userEmail);

    DecisionDetailAnalyticsResponseDto getDecisionDetailAnalytics(Long decisionId, String userEmail);
}
