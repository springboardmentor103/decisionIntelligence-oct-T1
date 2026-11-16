package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.OptionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionScoreRepository extends JpaRepository<OptionScore, Long> {

    List<OptionScore> findByOptionId(Long optionId);

    List<OptionScore> findByOptionDecisionId(Long decisionId);

    Optional<OptionScore> findByOptionIdAndCriterionId(Long optionId, Long criterionId);

    void deleteByCriterionId(Long criterionId);

    void deleteByOptionId(Long optionId);
}
