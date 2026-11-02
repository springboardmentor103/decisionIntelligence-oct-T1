package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Decision;
import com.decisionhub.backend.entity.DecisionStatus;
import com.decisionhub.backend.entity.DecisionVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}

