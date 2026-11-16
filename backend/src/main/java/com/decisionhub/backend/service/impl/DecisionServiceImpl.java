package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponseDto;
import com.decisionhub.backend.dto.OptionRequestDto;
import com.decisionhub.backend.dto.PagedResponseDto;
import com.decisionhub.backend.dto.UpdateDecisionRequest;
import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.PollType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.CommunityRepository;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.DecisionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class DecisionServiceImpl implements DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    public DecisionServiceImpl(
            DecisionRepository decisionRepository,
            UserRepository userRepository,
            CommunityRepository communityRepository) {
        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
    }

    @Override
    public DecisionResponseDto createDecision(CreateDecisionRequest request, String currentUserEmail) {
        User creator = getAuthenticatedUser(currentUserEmail);

        Community community = null;
        if (request.communityId() != null) {
            community = communityRepository.findById(request.communityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community", "id", request.communityId()));
        }

        Decision decision = Decision.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .category(request.category().trim())
                .visibility(request.visibility() != null ? request.visibility() : DecisionVisibility.PUBLIC)
                .status(request.status() != null ? request.status() : DecisionStatus.ACTIVE)
                .pollType(request.pollType() != null ? request.pollType() : PollType.SINGLE_CHOICE)
                .creator(creator)
                .community(community)
                .deadline(request.deadline())
                .options(new ArrayList<>())
                .build();

        if (request.options() != null && !request.options().isEmpty()) {
            int order = 0;
            for (OptionRequestDto optDto : request.options()) {
                if (optDto.title() != null && !optDto.title().isBlank()) {
                    Option option = Option.builder()
                            .decision(decision)
                            .title(optDto.title().trim())
                            .description(optDto.description() != null ? optDto.description().trim() : null)
                            .displayOrder(optDto.displayOrder() != null ? optDto.displayOrder() : order++)
                            .build();
                    decision.getOptions().add(option);
                }
            }
        }

        Decision saved = decisionRepository.save(decision);
        return DecisionResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<DecisionResponseDto> getPublicDecisions(Pageable pageable) {
        Page<Decision> decisions = decisionRepository.findByVisibility(DecisionVisibility.PUBLIC, pageable);
        Page<DecisionResponseDto> dtoPage = decisions.map(DecisionResponseDto::fromEntity);
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<DecisionResponseDto> getMyDecisions(String currentUserEmail, Pageable pageable) {
        User user = getAuthenticatedUser(currentUserEmail);
        Page<Decision> decisions = decisionRepository.findByCreatorId(user.getId(), pageable);
        Page<DecisionResponseDto> dtoPage = decisions.map(DecisionResponseDto::fromEntity);
        return PagedResponseDto.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public DecisionResponseDto getDecisionById(Long id, String currentUserEmail) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        validateAccess(decision, currentUserEmail);

        return DecisionResponseDto.fromEntity(decision);
    }

    @Override
    public DecisionResponseDto updateDecision(Long id, UpdateDecisionRequest request, String currentUserEmail) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        if (request.title() != null && !request.title().isBlank()) {
            decision.setTitle(request.title().trim());
        }
        if (request.description() != null && !request.description().isBlank()) {
            decision.setDescription(request.description().trim());
        }
        if (request.category() != null && !request.category().isBlank()) {
            decision.setCategory(request.category().trim());
        }
        if (request.visibility() != null) {
            decision.setVisibility(request.visibility());
        }
        if (request.status() != null) {
            decision.setStatus(request.status());
        }
        if (request.pollType() != null) {
            decision.setPollType(request.pollType());
        }
        if (request.deadline() != null) {
            decision.setDeadline(request.deadline());
        }
        if (request.communityId() != null) {
            Community community = communityRepository.findById(request.communityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Community", "id", request.communityId()));
            decision.setCommunity(community);
        }

        Decision updated = decisionRepository.save(decision);
        return DecisionResponseDto.fromEntity(updated);
    }

    @Override
    public void deleteDecision(Long id, String currentUserEmail) {
        Decision decision = decisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", id));

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        decisionRepository.delete(decision);
    }

    private User getAuthenticatedUser(String email) {
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }
        return user;
    }

    private void validateAccess(Decision decision, String currentUserEmail) {
        if (decision.getVisibility() == DecisionVisibility.PUBLIC) {
            return;
        }

        // Private decision access restriction
        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new UnauthorizedException("Authentication required to access private decision");
        }

        User user = getAuthenticatedUser(currentUserEmail);
        boolean isCreator = decision.getCreator().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ROLE_ADMIN;

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("You do not have permission to access this private decision");
        }
    }

    private void validateModificationPermission(Decision decision, User currentUser) {
        boolean isCreator = decision.getCreator().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ROLE_ADMIN;

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify or delete this decision");
        }
    }
}

