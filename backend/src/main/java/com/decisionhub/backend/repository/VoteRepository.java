package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing Vote entities.
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    List<Vote> findByDecisionId(Long decisionId);

    Optional<Vote> findByUserIdAndDecisionId(Long userId, Long decisionId);

    boolean existsByUserIdAndDecisionId(Long userId, Long decisionId);

    long countByOptionId(Long optionId);

    long countByDecisionId(Long decisionId);
}

