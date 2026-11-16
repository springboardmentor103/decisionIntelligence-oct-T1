package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.ComparisonDtos;
import com.decisionhub.backend.entity.*;
import com.decisionhub.backend.repository.*;
import com.decisionhub.backend.service.ComparisonService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComparisonServiceImpl implements ComparisonService {

    private final DecisionRepository decisionRepository;
    private final OptionRepository optionRepository;
    private final UserRepository userRepository;
    private final ComparisonCriteriaRepository criteriaRepository;
    private final OptionProsConsRepository prosConsRepository;
    private final OptionScoreRepository scoreRepository;

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

    private Option getOption(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Option not found"
                ));
    }

    private void checkOwner(Decision decision, User user) {
        if (!decision.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the decision creator can manage criteria"
            );
        }
    }

    private void checkDecisionAccess(Decision decision, User user) {
        if (decision.getVisibility() == DecisionVisibility.PRIVATE
                && !decision.getCreator().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this private decision"
            );
        }
    }

    @Override
    public ComparisonDtos.CriteriaResponse addCriteria(
            Long decisionId,
            ComparisonDtos.CreateCriteriaRequest request,
            String email
    ) {
        User user = getUser(email);
        Decision decision = getDecision(decisionId);

        checkOwner(decision, user);

        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Criteria name is required"
            );
        }

        if (criteriaRepository.existsByDecisionIdAndNameIgnoreCase(
                decisionId, request.name().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Criteria already exists"
            );
        }

        Double weight = request.weight() == null ? 1.0 : request.weight();

        if (weight <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Weight must be greater than zero"
            );
        }

        ComparisonCriteria criteria = new ComparisonCriteria();
        criteria.setDecision(decision);
        criteria.setName(request.name().trim());
        criteria.setDescription(request.description());
        criteria.setWeight(weight);

        ComparisonCriteria saved = criteriaRepository.save(criteria);

        return new ComparisonDtos.CriteriaResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getWeight()
        );
    }

    @Override
    public ComparisonDtos.ComparisonDetailsResponse getComparisonDetails(
            Long decisionId,
            String email
    ) {
        User user = getUser(email);
        Decision decision = getDecision(decisionId);

        checkDecisionAccess(decision, user);

        List<ComparisonDtos.CriteriaResponse> criteria =
                criteriaRepository.findByDecisionIdOrderByIdAsc(decisionId)
                        .stream()
                        .map(c -> new ComparisonDtos.CriteriaResponse(
                                c.getId(),
                                c.getName(),
                                c.getDescription(),
                                c.getWeight()
                        ))
                        .toList();

        List<Option> options =
                optionRepository.findByDecisionIdOrderByDisplayOrderAsc(
                        decisionId
                );

        List<ComparisonDtos.OptionComparisonResponse> optionResponses =
                options.stream().map(option -> {

                    List<ComparisonDtos.ProsConsResponse> prosCons =
                            prosConsRepository
                                    .findByOptionIdOrderByCreatedAtAsc(
                                            option.getId()
                                    )
                                    .stream()
                                    .map(p -> new ComparisonDtos.ProsConsResponse(
                                            p.getId(),
                                            p.getType().name(),
                                            p.getContent(),
                                            p.getUser().getFullName()
                                    ))
                                    .toList();

                    List<ComparisonDtos.ScoreResponse> scores =
                            scoreRepository.findByOptionId(option.getId())
                                    .stream()
                                    .map(s -> new ComparisonDtos.ScoreResponse(
                                            s.getCriteria().getId(),
                                            s.getCriteria().getName(),
                                            s.getScore(),
                                            s.getUser().getFullName()
                                    ))
                                    .toList();

                    return new ComparisonDtos.OptionComparisonResponse(
                            option.getId(),
                            option.getTitle(),
                            option.getDescription(),
                            prosCons,
                            scores
                    );
                }).toList();

        return new ComparisonDtos.ComparisonDetailsResponse(
                decisionId, criteria, optionResponses
        );
    }

    @Override
    public ComparisonDtos.ProsConsResponse addProsCons(
            Long optionId,
            ComparisonDtos.AddProsConsRequest request,
            String email
    ) {
        User user = getUser(email);
        Option option = getOption(optionId);

        checkDecisionAccess(option.getDecision(), user);

        if (request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Content is required"
            );
        }

        OptionProsCons.Type type;

        try {
            type = OptionProsCons.Type.valueOf(
                    request.type().trim().toUpperCase()
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Type must be PRO or CON"
            );
        }

        OptionProsCons item = new OptionProsCons();
        item.setOption(option);
        item.setUser(user);
        item.setType(type);
        item.setContent(request.content().trim());

        OptionProsCons saved = prosConsRepository.save(item);

        return new ComparisonDtos.ProsConsResponse(
                saved.getId(),
                saved.getType().name(),
                saved.getContent(),
                user.getFullName()
        );
    }

    @Override
    public ComparisonDtos.ScoreResponse addScore(
            Long optionId,
            ComparisonDtos.AddScoreRequest request,
            String email
    ) {
        User user = getUser(email);
        Option option = getOption(optionId);

        checkDecisionAccess(option.getDecision(), user);

        if (request.score() == null
                || request.score() < 1
                || request.score() > 10) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Score must be between 1 and 10"
            );
        }

        ComparisonCriteria criteria =
                criteriaRepository.findById(request.criteriaId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Comparison criteria not found"
                        ));

        if (!criteria.getDecision().getId()
                .equals(option.getDecision().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Criteria and option must belong to the same decision"
            );
        }

        OptionScore score = scoreRepository
                .findByOptionIdAndCriteriaIdAndUserId(
                        optionId, criteria.getId(), user.getId()
                )
                .orElseGet(OptionScore::new);

        score.setOption(option);
        score.setCriteria(criteria);
        score.setUser(user);
        score.setScore(request.score());

        OptionScore saved = scoreRepository.save(score);

        return new ComparisonDtos.ScoreResponse(
                criteria.getId(),
                criteria.getName(),
                saved.getScore(),
                user.getFullName()
        );
    }

    @Override
    public List<ComparisonDtos.RankedOptionResponse> getRankedOptions(
            Long decisionId,
            String email
    ) {
        User user = getUser(email);
        Decision decision = getDecision(decisionId);

        checkDecisionAccess(decision, user);

        List<ComparisonCriteria> criteriaList =
                criteriaRepository.findByDecisionIdOrderByIdAsc(decisionId);

        List<Option> options =
                optionRepository.findByDecisionIdOrderByDisplayOrderAsc(
                        decisionId
                );

        List<ComparisonDtos.RankedOptionResponse> ranking =
                new ArrayList<>();

        for (Option option : options) {

            List<OptionScore> scores =
                    scoreRepository.findByOptionId(option.getId());

            Map<Long, List<OptionScore>> scoresByCriteria =
                    scores.stream().collect(Collectors.groupingBy(
                            s -> s.getCriteria().getId()
                    ));

            double weightedTotal = 0;
            double totalWeight = 0;

            for (ComparisonCriteria criteria : criteriaList) {

                List<OptionScore> criterionScores =
                        scoresByCriteria.get(criteria.getId());

                if (criterionScores == null || criterionScores.isEmpty()) {
                    continue;
                }

                double average = criterionScores.stream()
                        .mapToInt(OptionScore::getScore)
                        .average()
                        .orElse(0);

                weightedTotal += average * criteria.getWeight();
                totalWeight += criteria.getWeight();
            }

            double finalScore = totalWeight == 0
                    ? 0
                    : weightedTotal / totalWeight;

            ranking.add(new ComparisonDtos.RankedOptionResponse(
                    option.getId(),
                    option.getTitle(),
                    Math.round(finalScore * 100.0) / 100.0,
                    scores.size()
            ));
        }

        ranking.sort(
                Comparator.comparing(
                        ComparisonDtos.RankedOptionResponse::weightedScore
                ).reversed()
        );

        return ranking;
    }
}