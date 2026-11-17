package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.CommunityDtos;
import com.decisionhub.backend.service.CommunityService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityDtos.CommunityResponse createCommunity(
            @RequestBody CommunityDtos.CreateCommunityRequest request,
            Principal principal
    ) {
        return communityService.createCommunity(
                request, principal.getName()
        );
    }

    @GetMapping
    public List<CommunityDtos.CommunityResponse> getAllCommunities() {
        return communityService.getAllCommunities();
    }

    @GetMapping("/my")
    public List<CommunityDtos.CommunityResponse> getMyCommunities(
            Principal principal
    ) {
        return communityService.getMyCommunities(principal.getName());
    }

    @GetMapping("/{communityId}")
    public CommunityDtos.CommunityResponse getCommunityDetails(
            @PathVariable Long communityId
    ) {
        return communityService.getCommunityDetails(communityId);
    }

    @PutMapping("/{communityId}")
    public CommunityDtos.CommunityResponse updateCommunity(
            @PathVariable Long communityId,
            @RequestBody CommunityDtos.UpdateCommunityRequest request,
            Principal principal
    ) {
        return communityService.updateCommunity(
                communityId, request, principal.getName()
        );
    }

    @DeleteMapping("/{communityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommunity(
            @PathVariable Long communityId,
            Principal principal
    ) {
        communityService.deleteCommunity(
                communityId, principal.getName()
        );
    }

    @PostMapping("/{communityId}/join")
    public String joinCommunity(
            @PathVariable Long communityId,
            Principal principal
    ) {
        return communityService.joinCommunity(
                communityId, principal.getName()
        );
    }

    @PostMapping("/{communityId}/leave")
    public String leaveCommunity(
            @PathVariable Long communityId,
            Principal principal
    ) {
        return communityService.leaveCommunity(
                communityId, principal.getName()
        );
    }

    @GetMapping("/{communityId}/members")
    public List<CommunityDtos.CommunityMemberResponse> getCommunityMembers(
            @PathVariable Long communityId
    ) {
        return communityService.getCommunityMembers(communityId);
    }
}