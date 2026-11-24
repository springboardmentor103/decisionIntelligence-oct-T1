package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Comment;
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
 * Spring Data JPA Repository for managing Comment entities.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByDecisionId(Long decisionId);

    List<Comment> findByDecisionIdAndParentCommentIsNull(Long decisionId);

    Page<Comment> findByDecisionIdAndParentCommentIsNull(Long decisionId, Pageable pageable);

    List<Comment> findByParentCommentId(Long parentCommentId);

    Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable);

    List<Comment> findByUserId(Long userId);

    long countByDecisionId(Long decisionId);

    long countByDecisionIdAndParentCommentIsNull(Long decisionId);

    long countByDecisionIdAndParentCommentIsNotNull(Long decisionId);

    long countByParentCommentId(Long parentCommentId);

    @Query("SELECT MAX(c.createdAt) FROM Comment c WHERE c.decision.id = :decisionId")
    Optional<LocalDateTime> findLatestActivityByDecisionId(@Param("decisionId") Long decisionId);

    @Query("SELECT c.parentComment.id, COUNT(c) FROM Comment c WHERE c.parentComment.id IN :parentIds GROUP BY c.parentComment.id")
    List<Object[]> countRepliesForParentIds(@Param("parentIds") List<Long> parentIds);

    long countByParentCommentIsNull();

    long countByParentCommentIsNotNull();

    @Query("SELECT c.decision.id, c.decision.title, COUNT(c) FROM Comment c WHERE c.deleted = false GROUP BY c.decision.id, c.decision.title ORDER BY COUNT(c) DESC")
    List<Object[]> findTopDiscussedDecisions(Pageable pageable);

    @Query("SELECT FUNCTION('DATE', c.createdAt), COUNT(c) FROM Comment c WHERE c.deleted = false GROUP BY FUNCTION('DATE', c.createdAt) ORDER BY FUNCTION('DATE', c.createdAt) ASC")
    List<Object[]> countCommentsDaily();
}

