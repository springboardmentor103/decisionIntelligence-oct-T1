package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Should successfully persist and retrieve a user with timestamps and default values")
    void shouldSaveAndRetrieveUserSuccessfully() {
        User user = User.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("$2a$10$hashedPasswordValueExample")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        entityManager.flush();

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Jane Doe");
        assertThat(savedUser.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(savedUser.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        Optional<User> found = userRepository.findByEmail("jane.doe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("Should enforce email uniqueness at the database level")
    void shouldFailWhenSavingDuplicateEmail() {
        User firstUser = User.builder()
                .fullName("First User")
                .email("unique@example.com")
                .password("secret123")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();
        userRepository.save(firstUser);
        entityManager.flush();

        User duplicateUser = User.builder()
                .fullName("Duplicate User")
                .email("unique@example.com")
                .password("differentSecret")
                .role(UserRole.ROLE_ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should verify existsByEmail for existing and non-existing emails")
    void shouldCheckIfEmailExists() {
        User user = User.builder()
                .fullName("Check User")
                .email("exists@example.com")
                .password("secret123")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();
        userRepository.save(user);
        entityManager.flush();

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("doesnotexist@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should filter users by role")
    void shouldFindUsersByRole() {
        User admin = User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password("adminSecret")
                .role(UserRole.ROLE_ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        User member = User.builder()
                .fullName("Regular Member")
                .email("member@example.com")
                .password("memberSecret")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();

        User moderator = User.builder()
                .fullName("Community Mod")
                .email("mod@example.com")
                .password("modSecret")
                .role(UserRole.ROLE_COMMUNITY_MODERATOR)
                .status(AccountStatus.ACTIVE)
                .build();

        userRepository.save(admin);
        userRepository.save(member);
        userRepository.save(moderator);
        entityManager.flush();

        List<User> admins = userRepository.findByRole(UserRole.ROLE_ADMIN);
        assertThat(admins).extracting(User::getEmail).contains("admin@example.com");

        List<User> mods = userRepository.findByRole(UserRole.ROLE_COMMUNITY_MODERATOR);
        assertThat(mods).extracting(User::getEmail).contains("mod@example.com");
    }

    @Test
    @DisplayName("Should filter users by account status")
    void shouldFindUsersByStatus() {
        User suspendedUser = User.builder()
                .fullName("Suspended User")
                .email("suspended@example.com")
                .password("pass123")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.SUSPENDED)
                .build();

        userRepository.save(suspendedUser);
        entityManager.flush();

        List<User> suspended = userRepository.findByStatus(AccountStatus.SUSPENDED);
        assertThat(suspended).extracting(User::getEmail).contains("suspended@example.com");
    }

    @Test
    @DisplayName("Should update user fields successfully")
    void shouldUpdateUserSuccessfully() {
        User user = User.builder()
                .fullName("Old Name")
                .email("update@example.com")
                .password("oldPassword")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.PENDING)
                .build();
        User saved = userRepository.save(user);
        entityManager.flush();

        saved.setFullName("New Name");
        saved.setStatus(AccountStatus.ACTIVE);
        saved.setRole(UserRole.ROLE_COMMUNITY_MODERATOR);
        User updated = userRepository.save(saved);
        entityManager.flush();

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getFullName()).isEqualTo("New Name");
        assertThat(reloaded.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(reloaded.getRole()).isEqualTo(UserRole.ROLE_COMMUNITY_MODERATOR);
    }
}

