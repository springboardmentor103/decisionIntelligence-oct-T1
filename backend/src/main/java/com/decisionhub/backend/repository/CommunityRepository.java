package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing Community entities.
 */
@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByName(String name);

    boolean existsByName(String name);

    List<Community> findByCreatorId(Long creatorId);
}

