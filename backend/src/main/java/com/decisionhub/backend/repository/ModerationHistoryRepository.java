package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.ModerationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for managing ModerationHistory entities.
 */
@Repository
public interface ModerationHistoryRepository extends JpaRepository<ModerationHistory, Long> {

    List<ModerationHistory> findByCommunityIdOrderByCreatedAtDesc(Long communityId);

    Page<ModerationHistory> findByCommunityIdOrderByCreatedAtDesc(Long communityId, Pageable pageable);

    List<ModerationHistory> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
}
