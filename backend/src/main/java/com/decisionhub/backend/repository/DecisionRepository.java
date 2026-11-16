package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing Decision entities.
 */
@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {

    List<Decision> findByCreatorId(Long creatorId);

    List<Decision> findByCommunityId(Long communityId);

    List<Decision> findByStatus(DecisionStatus status);

    List<Decision> findByCategory(String category);

    List<Decision> findByVisibility(DecisionVisibility visibility);

    @EntityGraph(attributePaths = {"creator", "community"})
    Page<Decision> findByVisibility(DecisionVisibility visibility, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "community"})
    Page<Decision> findByCreatorId(Long creatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "community"})
    Page<Decision> findByCreatorEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = {"creator", "community", "options"})
    Optional<Decision> findWithDetailsById(Long id);
}

