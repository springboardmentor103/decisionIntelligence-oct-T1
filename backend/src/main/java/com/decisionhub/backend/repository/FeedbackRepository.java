package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Feedback;
import com.decisionhub.backend.entity.FeedbackStatus;
import com.decisionhub.backend.entity.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing Feedback entities.
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findByDecisionIdAndDeletedFalse(Long decisionId, Pageable pageable);

    Page<Feedback> findByDecisionIdAndFeedbackTypeAndDeletedFalse(Long decisionId, FeedbackType feedbackType, Pageable pageable);

    long countByDecisionIdAndDeletedFalse(Long decisionId);

    @Query("SELECT f.feedbackType, COUNT(f) FROM Feedback f WHERE f.decision.id = :decisionId AND f.deleted = false GROUP BY f.feedbackType")
    List<Object[]> countByDecisionIdGroupByFeedbackType(@Param("decisionId") Long decisionId);

    @Query("SELECT f.status, COUNT(f) FROM Feedback f WHERE f.decision.id = :decisionId AND f.deleted = false GROUP BY f.status")
    List<Object[]> countByDecisionIdGroupByStatus(@Param("decisionId") Long decisionId);

    @Query("SELECT MAX(f.updatedAt) FROM Feedback f WHERE f.decision.id = :decisionId AND f.deleted = false")
    Optional<LocalDateTime> findLatestActivityByDecisionId(@Param("decisionId") Long decisionId);
}
