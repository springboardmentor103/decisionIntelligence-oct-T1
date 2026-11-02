package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for managing Comment entities.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByDecisionId(Long decisionId);

    List<Comment> findByDecisionIdAndParentCommentIsNull(Long decisionId);

    List<Comment> findByParentCommentId(Long parentCommentId);

    List<Comment> findByUserId(Long userId);
}

