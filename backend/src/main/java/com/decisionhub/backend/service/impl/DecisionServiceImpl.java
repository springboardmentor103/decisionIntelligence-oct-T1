
package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponse;
import com.decisionhub.backend.dto.OptionRequest;
import com.decisionhub.backend.dto.OptionResponse;

import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.User;

import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.OptionRepository;
import com.decisionhub.backend.repository.UserRepository;

import com.decisionhub.backend.service.DecisionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DecisionServiceImpl implements DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;

    public DecisionServiceImpl(
            DecisionRepository decisionRepository,
            UserRepository userRepository,
            OptionRepository optionRepository) {

        this.decisionRepository = decisionRepository;
        this.userRepository = userRepository;
        this.optionRepository = optionRepository;
    }

    // Helper: Find logged-in user
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    // Helper: Find decision
    private Decision getDecision(Long id) {
        return decisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"));
    }

    // Helper: Check decision ownership
    private void checkOwnership(Decision decision, User user) {
        if (!decision.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to modify this decision");
        }
    }

    // Helper: Check visibility
    private void checkVisibility(Decision decision, User user) {
        if (decision.getVisibility() == DecisionVisibility.PRIVATE
                && !decision.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this private decision");
        }
    }

    // Helper: Convert Decision entity to response
    private DecisionResponse mapToResponse(Decision decision) {

        return new DecisionResponse(
                decision.getId(),
                decision.getTitle(),
                decision.getDescription(),
                decision.getCategory(),
                decision.getVisibility(),
                decision.getStatus(),
                decision.getPollType(),
                decision.getCreator().getId(),
                decision.getCreator().getFullName(),
                decision.getCommunity() == null
                        ? null : decision.getCommunity().getId(),
                decision.getDeadline(),
                decision.getCreatedAt(),
                decision.getUpdatedAt(),
                decision.isCommunityPoll()
        );
    }

    // API 1: Create Decision
    @Override
    @Transactional
    public DecisionResponse createDecision(
            CreateDecisionRequest request,
            String userEmail) {

        User creator = getUser(userEmail);

        Decision decision = Decision.builder()
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .visibility(request.visibility())
                .status(DecisionStatus.ACTIVE)
                .pollType(request.pollType())
                .communityPoll(Boolean.TRUE.equals(request.communityPoll()))
                .deadline(request.deadline())
                .creator(creator)
                .build();

        return mapToResponse(decisionRepository.save(decision));
    }

    // API 2: Retrieve Public Decisions
    @Override
    @Transactional(readOnly = true)
    public List<DecisionResponse> getPublicDecisions() {

        return decisionRepository
                .findByVisibility(DecisionVisibility.PUBLIC)
                .stream()
                .filter(d -> d.getStatus() == DecisionStatus.ACTIVE)
                .map(this::mapToResponse)
                .toList();
    }
    // API: Retrieve Community Polls
@Override
@Transactional(readOnly = true)
public List<DecisionResponse> getCommunityPolls() {

    return decisionRepository
            .findByCommunityPollTrueAndVisibilityAndStatus(
                    DecisionVisibility.PUBLIC,
                    DecisionStatus.ACTIVE
            )
            .stream()
            .map(this::mapToResponse)
            .toList();
}

    // API 3: Retrieve My Decisions
    @Override
    @Transactional(readOnly = true)
    public List<DecisionResponse> getMyDecisions(String userEmail) {

        User user = getUser(userEmail);

        return decisionRepository.findByCreatorId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // API 4: Retrieve Decision Details
    @Override
    @Transactional(readOnly = true)
    public DecisionResponse getDecisionById(
            Long decisionId,
            String userEmail) {

        Decision decision = getDecision(decisionId);
        User user = getUser(userEmail);

        checkVisibility(decision, user);

        return mapToResponse(decision);
    }

    // API 5: Update Decision
    @Override
    @Transactional
    public DecisionResponse updateDecision(
            Long decisionId,
            CreateDecisionRequest request,
            String userEmail) {

        Decision decision = getDecision(decisionId);
        User user = getUser(userEmail);

        checkOwnership(decision, user);

        decision.setTitle(request.title());
        decision.setDescription(request.description());
        decision.setCategory(request.category());
        decision.setVisibility(request.visibility());
        decision.setPollType(request.pollType());
        if (request.communityPoll() != null) {
    decision.setCommunityPoll(request.communityPoll());
}
        decision.setDeadline(request.deadline());

        return mapToResponse(decisionRepository.save(decision));
    }

    // API 6: Delete Decision
    @Override
    @Transactional
    public void deleteDecision(Long decisionId, String userEmail) {

        Decision decision = getDecision(decisionId);
        User user = getUser(userEmail);

        checkOwnership(decision, user);

        decisionRepository.delete(decision);
    }

    // API 7: Add Option
    @Override
    @Transactional
    public OptionResponse addOption(
            Long decisionId,
            OptionRequest request,
            String userEmail) {

        Decision decision = getDecision(decisionId);
        User user = getUser(userEmail);

        checkOwnership(decision, user);

        Option option = Option.builder()
                .decision(decision)
                .title(request.title())
                .description(request.description())
                .displayOrder(
                        request.displayOrder() == null
                                ? 0 : request.displayOrder())
                .build();

        Option savedOption = optionRepository.save(option);

        return mapOptionToResponse(savedOption);
    }

    // API 8: Retrieve Decision Options
    @Override
    @Transactional(readOnly = true)
    public List<OptionResponse> getDecisionOptions(
            Long decisionId,
            String userEmail) {

        Decision decision = getDecision(decisionId);
        User user = getUser(userEmail);

        checkVisibility(decision, user);

        return optionRepository
                .findByDecisionIdOrderByDisplayOrderAsc(decisionId)
                .stream()
                .map(this::mapOptionToResponse)
                .toList();
    }

    // Helper: Convert Option entity to response
    private OptionResponse mapOptionToResponse(Option option) {

        return new OptionResponse(
                option.getId(),
                option.getDecision().getId(),
                option.getTitle(),
                option.getDescription(),
                option.getDisplayOrder(),
                option.getCreatedAt(),
                option.getUpdatedAt()
        );
    }
}
