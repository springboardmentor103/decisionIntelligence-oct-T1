package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.ComparisonMatrixResponseDto;
import com.decisionhub.backend.dto.CreateCriteriaRequest;
import com.decisionhub.backend.dto.CreateProConRequest;
import com.decisionhub.backend.dto.CriteriaResponseDto;
import com.decisionhub.backend.dto.CriterionScoreDetailDto;
import com.decisionhub.backend.dto.OptionComparisonRequest;
import com.decisionhub.backend.dto.OptionComparisonResponseDto;
import com.decisionhub.backend.dto.OptionScoreBatchRequest;
import com.decisionhub.backend.dto.OptionScoreResponseDto;
import com.decisionhub.backend.dto.ProConResponseDto;
import com.decisionhub.backend.dto.RankedOptionDto;
import com.decisionhub.backend.dto.SetOptionScoreRequest;
import com.decisionhub.backend.dto.UpdateCriteriaRequest;
import com.decisionhub.backend.dto.UpdateProConRequest;
import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.ComparisonCriteria;
import com.decisionhub.backend.entity.CriterionPolarity;
import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionVisibility;
import com.decisionhub.backend.entity.Option;
import com.decisionhub.backend.entity.OptionComparison;
import com.decisionhub.backend.entity.OptionProsCons;
import com.decisionhub.backend.entity.OptionScore;
import com.decisionhub.backend.entity.ProConType;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.ResourceNotFoundException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.ComparisonCriteriaRepository;
import com.decisionhub.backend.repository.DecisionRepository;
import com.decisionhub.backend.repository.OptionComparisonRepository;
import com.decisionhub.backend.repository.OptionProsConsRepository;
import com.decisionhub.backend.repository.OptionRepository;
import com.decisionhub.backend.repository.OptionScoreRepository;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.service.ComparisonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ComparisonServiceImpl implements ComparisonService {

    private final DecisionRepository decisionRepository;
    private final OptionRepository optionRepository;
    private final ComparisonCriteriaRepository criteriaRepository;
    private final OptionScoreRepository optionScoreRepository;
    private final OptionComparisonRepository optionComparisonRepository;
    private final OptionProsConsRepository optionProsConsRepository;
    private final UserRepository userRepository;

    public ComparisonServiceImpl(
            DecisionRepository decisionRepository,
            OptionRepository optionRepository,
            ComparisonCriteriaRepository criteriaRepository,
            OptionScoreRepository optionScoreRepository,
            OptionComparisonRepository optionComparisonRepository,
            OptionProsConsRepository optionProsConsRepository,
            UserRepository userRepository) {
        this.decisionRepository = decisionRepository;
        this.optionRepository = optionRepository;
        this.criteriaRepository = criteriaRepository;
        this.optionScoreRepository = optionScoreRepository;
        this.optionComparisonRepository = optionComparisonRepository;
        this.optionProsConsRepository = optionProsConsRepository;
        this.userRepository = userRepository;
    }

    // =========================================================================
    // 1. Comparison Criteria Management
    // =========================================================================

    @Override
    public CriteriaResponseDto createCriteria(Long decisionId, CreateCriteriaRequest request, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        String trimmedName = request.name().trim();
        if (criteriaRepository.existsByDecisionIdAndNameIgnoreCase(decisionId, trimmedName)) {
            throw new BadRequestException("A criterion with the name '" + trimmedName + "' already exists for this decision");
        }

        double minScore = request.minScore() != null ? request.minScore() : 1.0;
        double maxScore = request.maxScore() != null ? request.maxScore() : 10.0;
        if (maxScore <= minScore) {
            throw new BadRequestException("Maximum score must be strictly greater than minimum score");
        }

        double weight = request.weight() != null ? request.weight() : 20.0;
        if (weight <= 0) {
            throw new BadRequestException("Criterion weight must be strictly positive");
        }

        ComparisonCriteria criteria = ComparisonCriteria.builder()
                .decision(decision)
                .name(trimmedName)
                .description(request.description() != null ? request.description().trim() : null)
                .weight(weight)
                .minScore(minScore)
                .maxScore(maxScore)
                .polarity(request.polarity() != null ? request.polarity() : CriterionPolarity.HIGHER_IS_BETTER)
                .isDefault(false)
                .build();

        ComparisonCriteria saved = criteriaRepository.save(criteria);
        return CriteriaResponseDto.fromEntity(saved);
    }

    @Override
    public List<CriteriaResponseDto> createDefaultCriteria(Long decisionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        // Standard 5 default criteria with appropriate weights and polarities
        List<ComparisonCriteria> defaults = List.of(
                ComparisonCriteria.builder()
                        .decision(decision)
                        .name("COST")
                        .description("Financial costs, licensing fees, and implementation expenses")
                        .weight(20.0)
                        .minScore(1.0)
                        .maxScore(10.0)
                        .polarity(CriterionPolarity.LOWER_IS_BETTER)
                        .isDefault(true)
                        .build(),
                ComparisonCriteria.builder()
                        .decision(decision)
                        .name("BENEFITS")
                        .description("Direct strategic advantages, business capabilities, and quality gains")
                        .weight(25.0)
                        .minScore(1.0)
                        .maxScore(10.0)
                        .polarity(CriterionPolarity.HIGHER_IS_BETTER)
                        .isDefault(true)
                        .build(),
                ComparisonCriteria.builder()
                        .decision(decision)
                        .name("RISK")
                        .description("Technical debt, security exposure, vendor lock-in, and operational risks")
                        .weight(20.0)
                        .minScore(1.0)
                        .maxScore(10.0)
                        .polarity(CriterionPolarity.LOWER_IS_BETTER)
                        .isDefault(true)
                        .build(),
                ComparisonCriteria.builder()
                        .decision(decision)
                        .name("TIME")
                        .description("Implementation duration, ramp-up time, and maintenance overhead")
                        .weight(15.0)
                        .minScore(1.0)
                        .maxScore(10.0)
                        .polarity(CriterionPolarity.LOWER_IS_BETTER)
                        .isDefault(true)
                        .build(),
                ComparisonCriteria.builder()
                        .decision(decision)
                        .name("CONVENIENCE")
                        .description("Ease of setup, developer ergonomics, usability, and integration friendliness")
                        .weight(20.0)
                        .minScore(1.0)
                        .maxScore(10.0)
                        .polarity(CriterionPolarity.HIGHER_IS_BETTER)
                        .isDefault(true)
                        .build()
        );

        List<ComparisonCriteria> savedCriteria = new ArrayList<>();
        for (ComparisonCriteria def : defaults) {
            if (!criteriaRepository.existsByDecisionIdAndNameIgnoreCase(decisionId, def.getName())) {
                savedCriteria.add(criteriaRepository.save(def));
            }
        }

        List<ComparisonCriteria> all = criteriaRepository.findByDecisionIdOrderByIdAsc(decisionId);
        return all.stream().map(CriteriaResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CriteriaResponseDto> getCriteriaByDecisionId(Long decisionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        validateAccess(decision, currentUserEmail);

        List<ComparisonCriteria> criteria = criteriaRepository.findByDecisionIdOrderByIdAsc(decisionId);
        return criteria.stream().map(CriteriaResponseDto::fromEntity).toList();
    }

    @Override
    public CriteriaResponseDto updateCriteria(Long decisionId, Long criteriaId, UpdateCriteriaRequest request, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        ComparisonCriteria criteria = findCriteriaOrThrow(criteriaId);
        validateCriterionBelongsToDecision(criteria, decisionId);

        if (request.name() != null && !request.name().isBlank()) {
            String newName = request.name().trim();
            if (!newName.equalsIgnoreCase(criteria.getName()) &&
                criteriaRepository.existsByDecisionIdAndNameIgnoreCase(decisionId, newName)) {
                throw new BadRequestException("A criterion with the name '" + newName + "' already exists for this decision");
            }
            criteria.setName(newName);
        }

        if (request.description() != null) {
            criteria.setDescription(request.description().trim());
        }

        if (request.weight() != null) {
            if (request.weight() <= 0) {
                throw new BadRequestException("Criterion weight must be strictly positive");
            }
            criteria.setWeight(request.weight());
        }

        double min = request.minScore() != null ? request.minScore() : criteria.getMinScore();
        double max = request.maxScore() != null ? request.maxScore() : criteria.getMaxScore();
        if (max <= min) {
            throw new BadRequestException("Maximum score must be strictly greater than minimum score");
        }
        criteria.setMinScore(min);
        criteria.setMaxScore(max);

        if (request.polarity() != null) {
            criteria.setPolarity(request.polarity());
        }

        ComparisonCriteria updated = criteriaRepository.save(criteria);
        return CriteriaResponseDto.fromEntity(updated);
    }

    @Override
    public void deleteCriteria(Long decisionId, Long criteriaId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        ComparisonCriteria criteria = findCriteriaOrThrow(criteriaId);
        validateCriterionBelongsToDecision(criteria, decisionId);

        // Remove any scores linked to this criterion
        optionScoreRepository.deleteByCriterionId(criteriaId);
        criteriaRepository.delete(criteria);
    }

    // =========================================================================
    // 2. Qualitative Option Comparison Data
    // =========================================================================

    @Override
    public OptionComparisonResponseDto upsertOptionComparison(Long decisionId, Long optionId, OptionComparisonRequest request, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        OptionComparison comparison = optionComparisonRepository.findByOptionId(optionId)
                .orElseGet(() -> OptionComparison.builder().option(option).build());

        comparison.setFeatureValues(request.featureValues());
        comparison.setCostDetails(request.costDetails());
        comparison.setBenefitDetails(request.benefitDetails());
        comparison.setRiskDetails(request.riskDetails());
        comparison.setTimeDetails(request.timeDetails());
        comparison.setConvenienceDetails(request.convenienceDetails());

        OptionComparison saved = optionComparisonRepository.save(comparison);
        return OptionComparisonResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OptionComparisonResponseDto getOptionComparison(Long decisionId, Long optionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);
        validateAccess(decision, currentUserEmail);

        OptionComparison comparison = optionComparisonRepository.findByOptionId(optionId)
                .orElse(null);
        return OptionComparisonResponseDto.fromEntity(comparison);
    }

    // =========================================================================
    // 3. Option Scores Management
    // =========================================================================

    @Override
    public List<OptionScoreResponseDto> setOptionScores(Long decisionId, Long optionId, OptionScoreBatchRequest request, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        if (request == null || request.scores() == null || request.scores().isEmpty()) {
            throw new BadRequestException("At least one score entry must be provided");
        }

        List<OptionScore> savedScores = new ArrayList<>();
        for (SetOptionScoreRequest item : request.scores()) {
            ComparisonCriteria criterion = findCriteriaOrThrow(item.criterionId());
            validateCriterionBelongsToDecision(criterion, decisionId);

            double score = item.rawScore();
            if (score < criterion.getMinScore() || score > criterion.getMaxScore()) {
                throw new BadRequestException(String.format(
                        "Score %.2f for criterion '%s' is outside valid range [%.2f, %.2f]",
                        score, criterion.getName(), criterion.getMinScore(), criterion.getMaxScore()
                ));
            }

            OptionScore optionScore = optionScoreRepository.findByOptionIdAndCriterionId(optionId, criterion.getId())
                    .orElseGet(() -> OptionScore.builder()
                            .option(option)
                            .criterion(criterion)
                            .build());

            optionScore.setRawScore(score);
            optionScore.setNotes(item.notes());
            savedScores.add(optionScoreRepository.save(optionScore));
        }

        return savedScores.stream().map(OptionScoreResponseDto::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OptionScoreResponseDto> getOptionScores(Long decisionId, Long optionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);
        validateAccess(decision, currentUserEmail);

        List<OptionScore> scores = optionScoreRepository.findByOptionId(optionId);
        return scores.stream().map(OptionScoreResponseDto::fromEntity).toList();
    }

    // =========================================================================
    // 4. Pros and Cons Management
    // =========================================================================

    @Override
    public ProConResponseDto addProCon(Long decisionId, Long optionId, CreateProConRequest request, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        Integer order = request.displayOrder();
        if (order == null) {
            List<OptionProsCons> existing = optionProsConsRepository.findByOptionIdOrderByDisplayOrderAsc(optionId);
            order = existing.isEmpty() ? 0 : existing.getLast().getDisplayOrder() + 1;
        }

        OptionProsCons proCon = OptionProsCons.builder()
                .option(option)
                .type(request.type())
                .description(request.description().trim())
                .displayOrder(order)
                .build();

        OptionProsCons saved = optionProsConsRepository.save(proCon);
        return ProConResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProConResponseDto> getProsAndCons(Long decisionId, Long optionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);
        validateAccess(decision, currentUserEmail);

        List<OptionProsCons> list = optionProsConsRepository.findByOptionIdOrderByDisplayOrderAsc(optionId);
        return list.stream().map(ProConResponseDto::fromEntity).toList();
    }

    @Override
    public ProConResponseDto updateProCon(Long decisionId, Long optionId, Long proConId, UpdateProConRequest request, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        OptionProsCons proCon = optionProsConsRepository.findById(proConId)
                .orElseThrow(() -> new ResourceNotFoundException("OptionProsCons", "id", proConId));
        validateProConBelongsToOption(proCon, optionId);

        if (request.type() != null) {
            proCon.setType(request.type());
        }
        if (request.description() != null && !request.description().isBlank()) {
            proCon.setDescription(request.description().trim());
        }
        if (request.displayOrder() != null) {
            proCon.setDisplayOrder(request.displayOrder());
        }

        OptionProsCons updated = optionProsConsRepository.save(proCon);
        return ProConResponseDto.fromEntity(updated);
    }

    @Override
    public void deleteProCon(Long decisionId, Long optionId, Long proConId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        Option option = findOptionOrThrow(optionId);
        validateOptionBelongsToDecision(option, decisionId);

        User currentUser = getAuthenticatedUser(currentUserEmail);
        validateModificationPermission(decision, currentUser);

        OptionProsCons proCon = optionProsConsRepository.findById(proConId)
                .orElseThrow(() -> new ResourceNotFoundException("OptionProsCons", "id", proConId));
        validateProConBelongsToOption(proCon, optionId);

        optionProsConsRepository.delete(proCon);
    }

    // =========================================================================
    // 5. Evaluation Matrix & Deterministic Rankings
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public ComparisonMatrixResponseDto getComparisonMatrix(Long decisionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        validateAccess(decision, currentUserEmail);

        List<ComparisonCriteria> criteria = criteriaRepository.findByDecisionIdOrderByIdAsc(decisionId);
        List<RankedOptionDto> rankedOptions = calculateRankings(decision, criteria);

        String winner = rankedOptions.isEmpty() ? null : rankedOptions.getFirst().title();

        List<CriteriaResponseDto> criteriaDtos = criteria.stream()
                .map(CriteriaResponseDto::fromEntity)
                .toList();

        return new ComparisonMatrixResponseDto(
                decision.getId(),
                decision.getTitle(),
                criteriaDtos,
                rankedOptions,
                winner,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankedOptionDto> getRankedOptions(Long decisionId, String currentUserEmail) {
        Decision decision = findDecisionOrThrow(decisionId);
        validateAccess(decision, currentUserEmail);

        List<ComparisonCriteria> criteria = criteriaRepository.findByDecisionIdOrderByIdAsc(decisionId);
        return calculateRankings(decision, criteria);
    }

    // =========================================================================
    // Calculation & Normalization Engine
    // =========================================================================

    private List<RankedOptionDto> calculateRankings(Decision decision, List<ComparisonCriteria> criteria) {
        List<Option> options = optionRepository.findByDecisionIdOrderByDisplayOrderAsc(decision.getId());
        if (options.isEmpty()) {
            return List.of();
        }

        // Calculate total criteria weight for weight normalization
        double totalWeight = criteria.stream()
                .mapToDouble(c -> c.getWeight() != null && c.getWeight() > 0 ? c.getWeight() : 1.0)
                .sum();
        if (totalWeight <= 0) {
            totalWeight = 1.0;
        }

        // Find the highest-weighted criterion ID for tie-breaking
        Long highestWeightCriterionId = criteria.stream()
                .max(Comparator.comparingDouble(c -> c.getWeight() != null ? c.getWeight() : 0.0))
                .map(ComparisonCriteria::getId)
                .orElse(null);

        // Preload scores, qualitative comparisons, and pros/cons
        List<OptionScore> allScores = optionScoreRepository.findByOptionDecisionId(decision.getId());
        Map<String, OptionScore> scoreMap = new HashMap<>();
        for (OptionScore score : allScores) {
            scoreMap.put(score.getOption().getId() + ":" + score.getCriterion().getId(), score);
        }

        List<OptionComparison> allComparisons = optionComparisonRepository.findByOptionDecisionId(decision.getId());
        Map<Long, OptionComparison> comparisonMap = new HashMap<>();
        for (OptionComparison comp : allComparisons) {
            comparisonMap.put(comp.getOption().getId(), comp);
        }

        List<OptionProsCons> allProsCons = optionProsConsRepository.findByOptionDecisionId(decision.getId());
        Map<Long, List<OptionProsCons>> prosConsMap = new HashMap<>();
        for (OptionProsCons pc : allProsCons) {
            prosConsMap.computeIfAbsent(pc.getOption().getId(), k -> new ArrayList<>()).add(pc);
        }

        List<EvaluatedOptionRecord> evaluatedOptions = new ArrayList<>();

        for (Option option : options) {
            double totalScore = 0.0;
            List<CriterionScoreDetailDto> scoreDetails = new ArrayList<>();
            Double highestCriterionNormalizedScore = 0.0;

            for (ComparisonCriteria criterion : criteria) {
                double rawWeight = criterion.getWeight() != null && criterion.getWeight() > 0 ? criterion.getWeight() : 1.0;
                double normalizedWeight = rawWeight / totalWeight;

                double min = criterion.getMinScore() != null ? criterion.getMinScore() : 1.0;
                double max = criterion.getMaxScore() != null ? criterion.getMaxScore() : 10.0;
                double range = max > min ? (max - min) : 1.0;

                OptionScore foundScore = scoreMap.get(option.getId() + ":" + criterion.getId());
                boolean isFallback = false;
                double rawScore;
                String notes = null;

                if (foundScore != null && foundScore.getRawScore() != null) {
                    rawScore = foundScore.getRawScore();
                    notes = foundScore.getNotes();
                } else {
                    // Fallback to neutral midpoint if score is missing
                    rawScore = (min + max) / 2.0;
                    isFallback = true;
                    notes = "Score not provided; defaulted to neutral midpoint";
                }

                // Standardized 0 to 10 scale normalization
                double normalizedScore;
                if (criterion.getPolarity() == CriterionPolarity.LOWER_IS_BETTER) {
                    // Lower is better (e.g. Cost, Risk, Time): lower raw score produces higher normalized score
                    normalizedScore = 10.0 * ((max - rawScore) / range);
                } else {
                    // Higher is better (e.g. Benefits, Quality, Convenience)
                    normalizedScore = 10.0 * ((rawScore - min) / range);
                }

                // Clamp between 0.0 and 10.0
                normalizedScore = Math.max(0.0, Math.min(10.0, normalizedScore));

                double contribution = normalizedWeight * normalizedScore;
                totalScore += contribution;

                if (highestWeightCriterionId != null && highestWeightCriterionId.equals(criterion.getId())) {
                    highestCriterionNormalizedScore = normalizedScore;
                }

                scoreDetails.add(new CriterionScoreDetailDto(
                        criterion.getId(),
                        criterion.getName(),
                        round(rawWeight, 2),
                        round(normalizedWeight, 4),
                        criterion.getPolarity(),
                        round(rawScore, 2),
                        round(normalizedScore, 2),
                        round(contribution, 3),
                        isFallback,
                        notes
                ));
            }

            double roundedTotalScore = round(totalScore, 2);
            double scorePercentage = round(roundedTotalScore * 10.0, 1);

            OptionComparison comp = comparisonMap.get(option.getId());
            OptionComparisonResponseDto compDto = OptionComparisonResponseDto.fromEntity(comp);

            List<OptionProsCons> optionProsCons = prosConsMap.getOrDefault(option.getId(), List.of());
            List<ProConResponseDto> pros = optionProsCons.stream()
                    .filter(pc -> pc.getType() == ProConType.PRO)
                    .sorted(Comparator.comparing(OptionProsCons::getDisplayOrder))
                    .map(ProConResponseDto::fromEntity)
                    .toList();

            List<ProConResponseDto> cons = optionProsCons.stream()
                    .filter(pc -> pc.getType() == ProConType.CON)
                    .sorted(Comparator.comparing(OptionProsCons::getDisplayOrder))
                    .map(ProConResponseDto::fromEntity)
                    .toList();

            evaluatedOptions.add(new EvaluatedOptionRecord(
                    option,
                    roundedTotalScore,
                    scorePercentage,
                    scoreDetails,
                    compDto,
                    pros,
                    cons,
                    highestCriterionNormalizedScore
            ));
        }

        // Deterministic Multi-Level Tie-Breaking Comparator:
        // 1. Total weighted score DESC
        // 2. Score on highest-weighted criterion DESC
        // 3. Option display order ASC
        // 4. Option ID ASC
        evaluatedOptions.sort((a, b) -> {
            int scoreCmp = Double.compare(b.totalScore, a.totalScore);
            if (scoreCmp != 0) {
                return scoreCmp;
            }
            int tieBreakerCmp = Double.compare(b.highestCriterionScore, a.highestCriterionScore);
            if (tieBreakerCmp != 0) {
                return tieBreakerCmp;
            }
            int orderCmp = Integer.compare(
                    a.option.getDisplayOrder() != null ? a.option.getDisplayOrder() : 0,
                    b.option.getDisplayOrder() != null ? b.option.getDisplayOrder() : 0
            );
            if (orderCmp != 0) {
                return orderCmp;
            }
            return Long.compare(a.option.getId(), b.option.getId());
        });

        List<RankedOptionDto> result = new ArrayList<>();
        int rank = 1;
        for (EvaluatedOptionRecord item : evaluatedOptions) {
            result.add(new RankedOptionDto(
                    rank++,
                    item.option.getId(),
                    item.option.getTitle(),
                    item.option.getDescription(),
                    item.option.getDisplayOrder(),
                    item.totalScore,
                    item.scorePercentage,
                    item.scoreDetails,
                    item.comparisonData,
                    item.pros,
                    item.cons
            ));
        }

        return result;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private record EvaluatedOptionRecord(
            Option option,
            double totalScore,
            double scorePercentage,
            List<CriterionScoreDetailDto> scoreDetails,
            OptionComparisonResponseDto comparisonData,
            List<ProConResponseDto> pros,
            List<ProConResponseDto> cons,
            double highestCriterionScore
    ) {
    }

    // =========================================================================
    // Security and Authorization Helpers
    // =========================================================================

    private Decision findDecisionOrThrow(Long decisionId) {
        return decisionRepository.findById(decisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Decision", "id", decisionId));
    }

    private Option findOptionOrThrow(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Option", "id", optionId));
    }

    private ComparisonCriteria findCriteriaOrThrow(Long criteriaId) {
        return criteriaRepository.findById(criteriaId)
                .orElseThrow(() -> new ResourceNotFoundException("ComparisonCriteria", "id", criteriaId));
    }

    private void validateOptionBelongsToDecision(Option option, Long decisionId) {
        if (option.getDecision() == null || !option.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException(
                    String.format("Option with id %d does not belong to decision %d", option.getId(), decisionId)
            );
        }
    }

    private void validateCriterionBelongsToDecision(ComparisonCriteria criterion, Long decisionId) {
        if (criterion.getDecision() == null || !criterion.getDecision().getId().equals(decisionId)) {
            throw new BadRequestException(
                    String.format("Criterion with id %d does not belong to decision %d", criterion.getId(), decisionId)
            );
        }
    }

    private void validateProConBelongsToOption(OptionProsCons proCon, Long optionId) {
        if (proCon.getOption() == null || !proCon.getOption().getId().equals(optionId)) {
            throw new BadRequestException(
                    String.format("ProCon item with id %d does not belong to option %d", proCon.getId(), optionId)
            );
        }
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

        if (currentUserEmail == null || currentUserEmail.isBlank()) {
            throw new UnauthorizedException("Authentication required to access private decision comparison data");
        }

        User user = getAuthenticatedUser(currentUserEmail);
        boolean isCreator = decision.getCreator().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == UserRole.ROLE_ADMIN;

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view comparison data for this private decision");
        }
    }

    private void validateModificationPermission(Decision decision, User currentUser) {
        boolean isCreator = decision.getCreator().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ROLE_ADMIN;

        if (!isCreator && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify comparison criteria or data for this decision");
        }
    }
}
