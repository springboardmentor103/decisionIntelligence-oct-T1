
package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.PollResultDtos;
import com.decisionhub.backend.entity.*;
import com.decisionhub.backend.repository.*;
import com.decisionhub.backend.service.PollResultService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PollResultServiceImpl implements PollResultService {

    private final DecisionRepository decisionRepository;
    private final UserRepository userRepository;
    private final OptionRepository optionRepository;
    private final BallotRepository ballotRepository;
    private final BallotSelectionRepository selectionRepository;

    @Override
    public PollResultDtos.PollResultResponse getPollResults(
            Long decisionId,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Decision not found"
                ));

        if (decision.getPollType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Poll not found"
            );
        }

        if (decision.getVisibility() == DecisionVisibility.PRIVATE
                && !decision.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view results of this private decision"
            );
        }

        List<Ballot> ballots =
                ballotRepository.findByDecisionId(decisionId);

        List<BallotSelection> selections =
                selectionRepository.findByDecisionId(decisionId);

        List<Option> options =
                optionRepository.findByDecisionIdOrderByDisplayOrderAsc(
                        decisionId
                );

        long totalBallots = ballots.size();
        long totalSelections = selections.size();

        List<PollResultDtos.OptionResult> results = new ArrayList<>();

        for (Option option : options) {

            List<BallotSelection> optionSelections =
                    selections.stream()
                            .filter(selection ->
                                    selection.getOption().getId()
                                            .equals(option.getId()))
                            .toList();

            long voteCount = optionSelections.size();

            double percentage = totalSelections == 0
                    ? 0.0
                    : (voteCount * 100.0) / totalSelections;

            Double averageRating = null;

            if (decision.getPollType() == PollType.RATING) {

                averageRating = optionSelections.stream()
                        .map(BallotSelection::getRating)
                        .filter(rating -> rating != null)
                        .mapToInt(Integer::intValue)
                        .average()
                        .stream()
                        .map(avg -> Math.round(avg * 100.0) / 100.0)
                        .findFirst()
                        .orElse(0.0);
            }

            results.add(new PollResultDtos.OptionResult(
                    option.getId(),
                    option.getTitle(),
                    voteCount,
                    Math.round(percentage * 100.0) / 100.0,
                    averageRating
            ));
        }

        List<Long> winnerOptionIds = new ArrayList<>();

        if (decision.getPollType() == PollType.RATING) {

            double highestRating = results.stream()
                    .map(PollResultDtos.OptionResult::averageRating)
                    .filter(rating -> rating != null)
                    .max(Double::compareTo)
                    .orElse(0.0);

            if (highestRating > 0) {
                results.stream()
                        .filter(result ->
                                result.averageRating() != null
                                && Double.compare(
                                        result.averageRating(),
                                        highestRating
                                ) == 0
                        )
                        .map(PollResultDtos.OptionResult::optionId)
                        .forEach(winnerOptionIds::add);
            }

            results.sort(
                    Comparator.comparingDouble(
                            (PollResultDtos.OptionResult result) ->
                                    result.averageRating() == null
                                            ? 0.0
                                            : result.averageRating()
                    ).reversed()
            );

        } else {

            long highestVotes = results.stream()
                    .mapToLong(PollResultDtos.OptionResult::voteCount)
                    .max()
                    .orElse(0);

            if (highestVotes > 0) {
                results.stream()
                        .filter(result ->
                                result.voteCount() == highestVotes
                        )
                        .map(PollResultDtos.OptionResult::optionId)
                        .forEach(winnerOptionIds::add);
            }

            results.sort(
                    Comparator.comparingLong(
                            PollResultDtos.OptionResult::voteCount
                    ).reversed()
            );
        }

        return new PollResultDtos.PollResultResponse(
                decision.getId(),
                decision.getTitle(),
                decision.getPollType().name(),
                totalBallots,
                totalSelections,
                winnerOptionIds,
                results
        );
    }
}
