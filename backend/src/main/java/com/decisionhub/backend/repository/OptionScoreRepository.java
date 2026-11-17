package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.OptionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OptionScoreRepository
        extends JpaRepository<OptionScore, Long> {

    Optional<OptionScore> findByOptionIdAndCriteriaIdAndUserId(
            Long optionId, Long criteriaId, Long userId
    );

    List<OptionScore> findByOptionId(Long optionId);

    List<OptionScore> findByCriteriaId(Long criteriaId);
}