package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CommunityDtos;
import com.decisionhub.backend.entity.*;
import com.decisionhub.backend.repository.*;
import com.decisionhub.backend.service.CommunityService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DecisionRepository decisionRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));
    }

    private Community getCommunity(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Community not found"
                ));
    }

    private void checkCreator(Community community, User user) {
        if (!community.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the community creator can perform this action"
            );
        }
    }

    private boolean isActive(CommunityMember member) {
        return member.getStatus() != null
                && member.getStatus().name().equals("ACTIVE");
    }

    private CommunityDtos.CommunityResponse mapCommunity(
        Community community
) {
    long memberCount = memberRepository
            .countByCommunityIdAndStatus(
                    community.getId(),
                    CommunityMembershipStatus.ACTIVE
            );

    long decisionCount = decisionRepository
            .countByCommunityId(community.getId());

    return new CommunityDtos.CommunityResponse(
            community.getId(),
            community.getName(),
            community.getDescription(),
            community.getCreator().getId(),
            community.getCreator().getFullName(),
            memberCount,
            decisionCount,
            community.getCreatedAt()
    );
}

    @Override
    public CommunityDtos.CommunityResponse createCommunity(
            CommunityDtos.CreateCommunityRequest request,
            String email
    ) {
        User user = getUser(email);

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Community name is required"
            );
        }

        if (communityRepository.existsByName(request.name().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Community name already exists"
            );
        }

        Community community = new Community();
        community.setName(request.name().trim());
        community.setDescription(request.description());
        community.setCreator(user);

        Community saved = communityRepository.save(community);

        CommunityMember member = new CommunityMember();
        member.setCommunity(saved);
        member.setUser(user);
        memberRepository.save(member);

        return mapCommunity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDtos.CommunityResponse> getAllCommunities() {
        return communityRepository.findAll()
                .stream()
                .map(this::mapCommunity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityDtos.CommunityResponse getCommunityDetails(
            Long communityId
    ) {
        return mapCommunity(getCommunity(communityId));
    }

    @Override
    public CommunityDtos.CommunityResponse updateCommunity(
            Long communityId,
            CommunityDtos.UpdateCommunityRequest request,
            String email
    ) {
        User user = getUser(email);
        Community community = getCommunity(communityId);

        checkCreator(community, user);

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();

            if (!newName.equalsIgnoreCase(community.getName())
                    && communityRepository.existsByName(newName)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Community name already exists"
                );
            }

            community.setName(newName);
        }

        if (request.description() != null) {
            community.setDescription(request.description());
        }

        return mapCommunity(communityRepository.save(community));
    }

    @Override
    public void deleteCommunity(Long communityId, String email) {
        User user = getUser(email);
        Community community = getCommunity(communityId);

        checkCreator(community, user);
        communityRepository.delete(community);
    }

    @Override
    public String joinCommunity(Long communityId, String email) {
        User user = getUser(email);
        Community community = getCommunity(communityId);

        var existing = memberRepository.findByCommunityIdAndUserId(
                communityId, user.getId()
        );

        if (existing.isPresent()) {
            CommunityMember member = existing.get();
            String status = member.getStatus().name();

            if (status.equals("BANNED")) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are banned from this community"
                );
            }

            if (status.equals("ACTIVE")) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "You are already a member"
                );
            }

            // Reactivate a member whose previous status was LEFT.
            memberRepository.delete(member);
        }

        CommunityMember newMember = new CommunityMember();
        newMember.setCommunity(community);
        newMember.setUser(user);
        memberRepository.save(newMember);

        return "Successfully joined the community";
    }

    @Override
    public String leaveCommunity(Long communityId, String email) {
        User user = getUser(email);
        getCommunity(communityId);

        CommunityMember member = memberRepository
                .findByCommunityIdAndUserId(communityId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "You are not a member of this community"
                ));

        if (!isActive(member)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You are not an active member"
            );
        }

        // Delete membership so the user can join again later.
        memberRepository.delete(member);

        return "Successfully left the community";
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDtos.CommunityMemberResponse> getCommunityMembers(
            Long communityId
    ) {
        getCommunity(communityId);

        return memberRepository.findByCommunityId(communityId)
                .stream()
                .filter(this::isActive)
                .map(member -> new CommunityDtos.CommunityMemberResponse(
                        member.getUser().getId(),
                        member.getUser().getFullName(),
                        member.getUser().getEmail(),
                        member.getRole().name(),
                        member.getStatus().name(),
                        member.getJoinedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityDtos.CommunityResponse> getMyCommunities(
            String email
    ) {
        User user = getUser(email);

        return memberRepository.findByUserId(user.getId())
                .stream()
                .filter(this::isActive)
                .map(member -> mapCommunity(member.getCommunity()))
                .toList();
    }
}