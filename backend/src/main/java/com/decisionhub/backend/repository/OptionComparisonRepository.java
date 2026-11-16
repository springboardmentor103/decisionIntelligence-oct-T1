package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.OptionComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionComparisonRepository extends JpaRepository<OptionComparison, Long> {

    Optional<OptionComparison> findByOptionId(Long optionId);

    List<OptionComparison> findByOptionDecisionId(Long decisionId);

    boolean existsByOptionId(Long optionId);
}
