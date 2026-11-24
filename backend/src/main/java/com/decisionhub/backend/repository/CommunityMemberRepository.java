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

    @org.springframework.data.jpa.repository.Query("SELECT cm.role, COUNT(cm) FROM CommunityMember cm GROUP BY cm.role")
    List<Object[]> countGroupByRole();

    @org.springframework.data.jpa.repository.Query("SELECT cm.status, COUNT(cm) FROM CommunityMember cm GROUP BY cm.status")
    List<Object[]> countGroupByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT c.id, c.name, COUNT(cm) FROM Community c LEFT JOIN c.members cm WHERE cm.status = 'ACTIVE' GROUP BY c.id, c.name ORDER BY COUNT(cm) DESC")
    List<Object[]> findTopCommunitiesByActiveMembers(org.springframework.data.domain.Pageable pageable);
}

