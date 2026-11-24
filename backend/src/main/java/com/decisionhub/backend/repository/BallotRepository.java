package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Ballot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing Ballot entities.
 */
@Repository
public interface BallotRepository extends JpaRepository<Ballot, Long> {

    List<Ballot> findByDecisionId(Long decisionId);

    List<Ballot> findByUserId(Long userId);

    Optional<Ballot> findByUserIdAndDecisionId(Long userId, Long decisionId);

    boolean existsByUserIdAndDecisionId(Long userId, Long decisionId);

    long countByDecisionId(Long decisionId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT b.decision.id) FROM Ballot b")
    Long countDistinctDecisionsWithBallots();

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('DATE', b.createdAt), COUNT(b) FROM Ballot b GROUP BY FUNCTION('DATE', b.createdAt) ORDER BY FUNCTION('DATE', b.createdAt) ASC")
    List<Object[]> countBallotsDaily();
}

