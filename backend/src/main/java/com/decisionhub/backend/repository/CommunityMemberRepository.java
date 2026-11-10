package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.CommunityMember;
import com.decisionhub.backend.entity.CommunityMembershipRole;
import com.decisionhub.backend.entity.CommunityMembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing CommunityMember entities.
 */
@Repository
public interface CommunityMemberRepository extends JpaRepository<CommunityMember, Long> {

    List<CommunityMember> findByCommunityId(Long communityId);

    List<CommunityMember> findByUserId(Long userId);

    Optional<CommunityMember> findByCommunityIdAndUserId(Long communityId, Long userId);

    boolean existsByCommunityIdAndUserId(Long communityId, Long userId);

    List<CommunityMember> findByCommunityIdAndRole(Long communityId, CommunityMembershipRole role);

    List<CommunityMember> findByCommunityIdAndStatus(Long communityId, CommunityMembershipStatus status);

    long countByCommunityIdAndStatus(Long communityId, CommunityMembershipStatus status);
}

