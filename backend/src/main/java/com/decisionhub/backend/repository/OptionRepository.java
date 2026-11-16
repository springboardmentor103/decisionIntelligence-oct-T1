package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing Option entities.
 */
@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    List<Option> findByDecisionId(Long decisionId);

    List<Option> findByDecisionIdOrderByDisplayOrderAsc(Long decisionId);

    Optional<Option> findByIdAndDecisionId(Long id, Long decisionId);

    boolean existsByIdAndDecisionId(Long id, Long decisionId);
}

