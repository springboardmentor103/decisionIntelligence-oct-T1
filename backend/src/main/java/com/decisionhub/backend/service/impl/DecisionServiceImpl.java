

package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.CreateDecisionRequest;
import com.decisionhub.backend.dto.DecisionResponse;
import com.decisionhub.backend.dto.OptionRequest;
import com.decisionhub.backend.dto.OptionResponse;
import com.decisionhub.backend.entity.Community;
import com.decisionhub.backend.repository.CommunityRepository;

import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.repository.VoteRepository;
import com.decisionhub.backend.repository.BallotSelectionRepository;

import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.OptionRepository;
import com.decisionhub.backend.repository.UserRepository;

import com.decisionhub.backend.service.DecisionService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.decisionhub.backend.dto.ComparisonCriteriaRequest;
import com.decisionhub.backend.entity.ComparisonCriteria;
import com.decisionhub.backend.repository.ComparisonCriteriaRepository;
import java.util.Map;
import java.util.HashMap;
import com.decisionhub.backend.dto.ComparisonCriteriaResponse;

import java.util.List;

@Service
public class DecisionServiceImpl implements DecisionService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final ComparisonCriteriaRepository comparisonCriteriaRepository;
    private final VoteRepository voteRepository;
private final BallotSelectionRepository ballotSelectionRepository;
private final CommunityRepository communityRepository;

    public DecisionServiceImpl(
        DecisionRepository decisionRepository,
        UserRepository userRepository,
        OptionRepository optionRepository,
        ComparisonCriteriaRepository comparisonCriteriaRepository,
        VoteRepository voteRepository,
        BallotSelectionRepository ballotSelectionRepository,
        CommunityRepository communityRepository) {

    this.decisionRepository = decisionRepository;
    this.userRepository = userRepository;
    this.optionRepository = optionRepository;
    this.comparisonCriteriaRepository = comparisonCriteriaRepository;
    this.voteRepository = voteRepository;
    this.ballotSelectionRepository = ballotSelectionRepository;
    this.communityRepository = communityRepository;
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
    // Helper: Save and synchronize evaluation criteria
private void synchronizeCriteria(
        Decision decision,
        List<ComparisonCriteriaRequest> requestedCriteria) {

    // If criteria are not sent, preserve the existing criteria
    if (requestedCriteria == null) {
        return;
    }

    List<ComparisonCriteria> existingCriteria =
            comparisonCriteriaRepository
                    .findByDecisionIdOrderByIdAsc(decision.getId());

    Map<String, ComparisonCriteria> existingByName = new HashMap<>();

    for (ComparisonCriteria criterion : existingCriteria) {
        existingByName.put(
                criterion.getName().toLowerCase(),
                criterion
        );
    }

    List<ComparisonCriteria> criteriaToSave = new java.util.ArrayList<>();

    for (ComparisonCriteriaRequest request : requestedCriteria) {

        String key = request.name().toLowerCase();

        ComparisonCriteria criterion = existingByName.remove(key);

        // Reuse existing criterion or create a new one
        if (criterion == null) {
            criterion = new ComparisonCriteria();
            criterion.setDecision(decision);
            criterion.setName(request.name());
        }

        criterion.setName(request.name());
        criterion.setDescription(request.description());
        criterion.setWeight(
                request.weight() == null ? 1.0 : request.weight()
        );

        criteriaToSave.add(criterion);
    }

    // Save new and updated criteria
    comparisonCriteriaRepository.saveAll(criteriaToSave);

    // Delete only criteria removed by the user
    comparisonCriteriaRepository.deleteAll(existingByName.values());
}

    // Helper: Synchronize decision options
    private void synchronizeOptions(
            Decision decision,
            List<OptionRequest> requestedOptions) {

        // If options are not sent, preserve existing options
        if (requestedOptions == null) {
            return;
        }

        List<Option> existingOptions =
                optionRepository.findByDecisionIdOrderByDisplayOrderAsc(
                        decision.getId()
                );

        Map<Long, Option> existingById = new HashMap<>();

        for (Option option : existingOptions) {
            existingById.put(option.getId(), option);
        }

        List<Option> optionsToSave = new java.util.ArrayList<>();

        for (int index = 0; index < requestedOptions.size(); index++) {

            OptionRequest optionRequest = requestedOptions.get(index);
            Option option;

            if (optionRequest.id() != null) {

                option = existingById.remove(optionRequest.id());

                if (option == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Invalid option ID for this decision"
                    );
                }

            } else {
                option = new Option();
                option.setDecision(decision);
            }

            option.setTitle(optionRequest.title());
            option.setDescription(optionRequest.description());
            option.setDisplayOrder(
                    optionRequest.displayOrder() == null
                            ? index
                            : optionRequest.displayOrder()
            );

            optionsToSave.add(option);
        }

        // Check whether removed options have voting history
        for (Option removedOption : existingById.values()) {

            long voteCount =
                    voteRepository.countByOptionId(removedOption.getId());

            long selectionCount =
                    ballotSelectionRepository.countByOptionId(
                            removedOption.getId()
                    );

            if (voteCount > 0 || selectionCount > 0) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot remove option '" + removedOption.getTitle()
                                + "' because it has existing votes or ballot selections."
                );
            }
        }

        // Save new and updated options
        optionRepository.saveAll(optionsToSave);

        // Delete options removed by the user
        optionRepository.deleteAll(existingById.values());
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

    Community community = null;

    if (request.communityId() != null) {
        community = communityRepository.findById(request.communityId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Community not found"));
    }

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
            .community(community)
            .build();

    Decision savedDecision = decisionRepository.save(decision);

    synchronizeCriteria(savedDecision, request.criteria());

    return mapToResponse(savedDecision);
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
        if (request.communityId() != null) {
    Community community = communityRepository.findById(request.communityId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Community not found"));

    decision.setCommunity(community);
}
        decision.setVisibility(request.visibility());
        decision.setPollType(request.pollType());
        if (request.communityPoll() != null) {
    decision.setCommunityPoll(request.communityPoll());
}
       decision.setDeadline(request.deadline());

Decision savedDecision = decisionRepository.save(decision);

synchronizeCriteria(savedDecision, request.criteria());

synchronizeOptions(savedDecision, request.options());

return mapToResponse(savedDecision);
    }

    // API 6: Delete Decision
@Override
@Transactional
public void deleteDecision(Long decisionId, String userEmail) {

    Decision decision = getDecision(decisionId);
    User user = getUser(userEmail);

    checkOwnership(decision, user);

    // Delete evaluation criteria linked to this decision first
    comparisonCriteriaRepository.deleteByDecisionId(decisionId);

    // Then delete the decision
    decisionRepository.delete(decision);
}
    // API 9: Retrieve Evaluation Criteria
@Override
@Transactional(readOnly = true)
public List<ComparisonCriteriaResponse> getDecisionCriteria(
        Long decisionId,
        String userEmail) {

    Decision decision = getDecision(decisionId);
    User user = getUser(userEmail);

    checkVisibility(decision, user);

    return comparisonCriteriaRepository
            .findByDecisionIdOrderByIdAsc(decisionId)
            .stream()
            .map(criterion -> new ComparisonCriteriaResponse(
                    criterion.getId(),
                    criterion.getDecision().getId(),
                    criterion.getName(),
                    criterion.getDescription(),
                    criterion.getWeight()
            ))
            .toList();
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
