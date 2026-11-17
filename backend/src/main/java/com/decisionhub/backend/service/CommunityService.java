package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.CommunityDtos;

import java.util.List;

public interface CommunityService {

    CommunityDtos.CommunityResponse createCommunity(
            CommunityDtos.CreateCommunityRequest request,
            String email
    );

    List<CommunityDtos.CommunityResponse> getAllCommunities();

    CommunityDtos.CommunityResponse getCommunityDetails(Long communityId);

    CommunityDtos.CommunityResponse updateCommunity(
            Long communityId,
            CommunityDtos.UpdateCommunityRequest request,
            String email
    );

    void deleteCommunity(Long communityId, String email);

    String joinCommunity(Long communityId, String email);

    String leaveCommunity(Long communityId, String email);

    List<CommunityDtos.CommunityMemberResponse> getCommunityMembers(
            Long communityId
    );

    List<CommunityDtos.CommunityResponse> getMyCommunities(String email);
}