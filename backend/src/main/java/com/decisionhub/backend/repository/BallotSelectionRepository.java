package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.BallotSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for managing BallotSelection entities.
 */
@Repository
public interface BallotSelectionRepository extends JpaRepository<BallotSelection, Long> {

    List<BallotSelection> findByBallotId(Long ballotId);

    List<BallotSelection> findByOptionId(Long optionId);

    List<BallotSelection> findByDecisionId(Long decisionId);

    long countByOptionId(Long optionId);
}

