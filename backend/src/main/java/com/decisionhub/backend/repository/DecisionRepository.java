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
    long countByCommunityId(Long communityId);

    List<Decision> findByStatus(DecisionStatus status);

    List<Decision> findByCategory(String category);

    List<Decision> findByVisibility(DecisionVisibility visibility);
    List<Decision> findByCommunityPollTrueAndVisibilityAndStatus( DecisionVisibility visibility,DecisionStatus status
);

    List<Decision> findByStatusAndDeadlineBetween(DecisionStatus status, java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<Decision> findByStatusAndDeadlineBefore(DecisionStatus status, java.time.LocalDateTime now);

    long countByStatus(DecisionStatus status);

    long countByVisibility(DecisionVisibility visibility);

    @org.springframework.data.jpa.repository.Query("SELECT d.category, COUNT(d) FROM Decision d GROUP BY d.category")
    List<Object[]> countGroupByCategory();

    @org.springframework.data.jpa.repository.Query("SELECT d.status, COUNT(d) FROM Decision d GROUP BY d.status")
    List<Object[]> countGroupByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT d.visibility, COUNT(d) FROM Decision d GROUP BY d.visibility")
    List<Object[]> countGroupByVisibility();

    @org.springframework.data.jpa.repository.Query("SELECT d.pollType, COUNT(d) FROM Decision d WHERE d.pollType IS NOT NULL GROUP BY d.pollType")
    List<Object[]> countGroupByPollType();

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('DATE', d.createdAt), COUNT(d) FROM Decision d GROUP BY FUNCTION('DATE', d.createdAt) ORDER BY FUNCTION('DATE', d.createdAt) ASC")
    List<Object[]> countCreatedDaily();
}

