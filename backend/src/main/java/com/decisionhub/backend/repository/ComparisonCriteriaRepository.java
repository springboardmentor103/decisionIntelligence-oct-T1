package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.ComparisonCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComparisonCriteriaRepository
        extends JpaRepository<ComparisonCriteria, Long> {

    List<ComparisonCriteria> findByDecisionIdOrderByIdAsc(Long decisionId);

    boolean existsByDecisionIdAndNameIgnoreCase(
            Long decisionId, String name
    );
}