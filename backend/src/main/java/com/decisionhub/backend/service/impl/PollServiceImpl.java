package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.PollDtos;
import com.decisionhub.backend.entity.*;
import com.decisionhub.backend.repository.*;
import com.decisionhub.backend.service.PollService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PollServiceImpl implements PollService {

    private final DecisionRepository decisionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;
    private final BallotRepository ballotRepository;
    private final BallotSelectionRepository selectionRepository;
    private final com.decisionhub.backend.service.NotificationService notificationService;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));
    }

    private Decision getDecision(Long decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"
                ));
    }

    private void checkOwner(Decision decision, User user) {
        if (!decision.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the decision creator can create a poll"
            );
        }
    }

    private void checkPollActive(Decision decision) {
        if (decision.getPollType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Poll has not been created for this decision"
            );
        }

        if (decision.getStatus() != DecisionStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This decision is not active"
            );
        }

        if (decision.getDeadline() != null
                && decision.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Poll deadline has passed"
            );
        }
    }

    private PollDtos.PollDetailsResponse mapPollDetails(
            Decision decision
    ) {
        List<PollDtos.OptionVoteCountResponse> options =
                optionRepository.findByDecisionIdOrderByDisplayOrderAsc(
                        decision.getId()
                )
                .stream()
                .map(option -> new PollDtos.OptionVoteCountResponse(
                        option.getId(),
                        option.getTitle(),
                        selectionRepository.countByOptionId(option.getId())
                ))
                .toList();

        boolean expired = decision.getDeadline() != null
                && decision.getDeadline().isBefore(LocalDateTime.now());

        return new PollDtos.PollDetailsResponse(
                decision.getId(),
                decision.getTitle(),
                decision.getPollType() == null
                        ? null : decision.getPollType().name(),
                decision.getStatus().name(),
                decision.getDeadline(),
                expired,
                options
        );
    }

    @Override
    public PollDtos.PollDetailsResponse createPoll(
            Long decisionId,
            PollDtos.CreatePollRequest request,
            String email
    ) {
        User user = getUser(email);
        Decision decision = getDecision(decisionId);

        checkOwner(decision, user);

        if (request.pollType() == null || request.pollType().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Poll type is required"
            );
        }

        PollType pollType;

        try {
            pollType = PollType.valueOf(
                    request.pollType().trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Poll type must be SINGLE_CHOICE, MULTIPLE_CHOICE or RATING"
            );
        }

        List<Option> options =
                optionRepository.findByDecisionIdOrderByDisplayOrderAsc(
                        decisionId
                );

        if (options.size() < 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least two options are required to create a poll"
            );
        }

        if (decision.getPollType() != null
                || ballotRepository.countByDecisionId(decisionId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Poll already exists for this decision"
            );
        }

        if (request.deadline() != null
                && request.deadline().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Deadline must be in the future"
            );
        }

        decision.setPollType(pollType);
        decision.setDeadline(request.deadline());

        Decision saved = decisionRepository.save(decision);

        return mapPollDetails(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PollDtos.PollDetailsResponse getPollDetails(Long decisionId) {
        Decision decision = getDecision(decisionId);

        if (decision.getPollType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Poll not found for this decision"
            );
        }

        return mapPollDetails(decision);
    }

    @Override
    public PollDtos.SubmitVoteResponse submitVote(
            Long decisionId,
            PollDtos.SubmitVoteRequest request,
            String email
    ) {
        User user = getUser(email);
        Decision decision = getDecision(decisionId);

        checkPollActive(decision);

        if (ballotRepository.existsByUserIdAndDecisionId(
                user.getId(), decisionId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already voted in this poll"
            );
        }

        if (request.selections() == null
                || request.selections().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one selection is required"
            );
        }

        Set<Long> selectedOptionIds = new HashSet<>();

        for (PollDtos.VoteSelectionRequest selection : request.selections()) {
            if (selection.optionId() == null
                    || !selectedOptionIds.add(selection.optionId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or duplicate option selection"
                );
            }

            Option option = optionRepository.findById(selection.optionId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Selected option not found"
                    ));

            if (!option.getDecision().getId().equals(decisionId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selected option does not belong to this decision"
                );
            }
        }

        PollType pollType = decision.getPollType();

        if (pollType == PollType.SINGLE_CHOICE
                && request.selections().size() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Single-choice poll requires exactly one option"
            );
        }

        if (pollType == PollType.RATING) {
            if (request.selections().size() != 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Rating poll requires exactly one option"
                );
            }

            Integer rating = request.selections().get(0).rating();

            if (rating == null || rating < 1 || rating > 5) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Rating must be between 1 and 5"
                );
            }
        }

        if (pollType != PollType.RATING) {
            for (PollDtos.VoteSelectionRequest selection
                    : request.selections()) {
                if (selection.rating() != null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Rating is only allowed for RATING polls"
                    );
                }
            }
        }

        Ballot ballot = new Ballot();
        ballot.setUser(user);
        ballot.setDecision(decision);

        Ballot savedBallot = ballotRepository.save(ballot);

        for (PollDtos.VoteSelectionRequest selection
                : request.selections()) {

            Option option = optionRepository.findById(
                    selection.optionId()
            ).orElseThrow();

            BallotSelection ballotSelection = new BallotSelection();
            ballotSelection.setBallot(savedBallot);
            ballotSelection.setDecision(decision);
            ballotSelection.setOption(option);
            ballotSelection.setRating(
                    pollType == PollType.RATING
                            ? selection.rating() : null
            );

            selectionRepository.save(ballotSelection);
        }

        // Notify decision creator if someone else voted
        if (!decision.getCreator().getId().equals(user.getId())) {
            notificationService.createNotification(
                    decision.getCreator(),
                    NotificationType.VOTE_RECEIVED,
                    "A new vote was submitted on your poll: '" + decision.getTitle() + "'",
                    decision.getId()
            );
        }

        return new PollDtos.SubmitVoteResponse(
                savedBallot.getId(),
                "Vote submitted successfully"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PollDtos.VoteStatusResponse checkVoteStatus(
            Long decisionId,
            String email
    ) {
        User user = getUser(email);
        Decision decision = getDecision(decisionId);

        if (decision.getPollType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Poll not found"
            );
        }

        boolean hasVoted = ballotRepository.existsByUserIdAndDecisionId(
                user.getId(), decisionId
        );

        return new PollDtos.VoteStatusResponse(
                decisionId, hasVoted
        );
    }
}