package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique email address.
     *
     * @param email user's email address
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email address already exists.
     *
     * @param email email to check
     * @return true if an account exists with this email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     *
     * @param role role to search by
     * @return list of matching users
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all users with a specific account status.
     *
     * @param status account status to search by
     * @return list of matching users
     */
    List<User> findByStatus(AccountStatus status);
}

