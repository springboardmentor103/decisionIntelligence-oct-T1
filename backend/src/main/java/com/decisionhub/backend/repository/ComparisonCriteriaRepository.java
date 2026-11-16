package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.ComparisonCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComparisonCriteriaRepository extends JpaRepository<ComparisonCriteria, Long> {

    List<ComparisonCriteria> findByDecisionId(Long decisionId);

    List<ComparisonCriteria> findByDecisionIdOrderByIdAsc(Long decisionId);

    Optional<ComparisonCriteria> findByIdAndDecisionId(Long id, Long decisionId);

    boolean existsByDecisionIdAndNameIgnoreCase(Long decisionId, String name);

    void deleteByDecisionId(Long decisionId);
}
