package com.decisionhub.backend.security;

import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // ==========================================
    // Public Endpoints Accessibility
    // ==========================================

    @Test
    @DisplayName("Public registration endpoint is accessible without authentication (returns validation error, not 401)")
    void shouldAllowUnauthenticatedAccessToRegistrationEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Public login endpoint is accessible without authentication (returns validation error, not 401)")
    void shouldAllowUnauthenticatedAccessToLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("Public health endpoint /api/v1/health is accessible without authentication")
    void shouldAllowUnauthenticatedAccessToHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ==========================================
    // Protected Endpoints Rejection
    // ==========================================

    @Test
    @DisplayName("Protected endpoint rejects unauthenticated requests with HTTP 401 Unauthorized")
    void shouldRejectUnauthenticatedRequestWithHttp401() throws Exception {
        mockMvc.perform(get("/api/v1/decisions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint rejects requests with invalid Bearer token with HTTP 401 Unauthorized")
    void shouldRejectInvalidBearerTokenWithHttp401() throws Exception {
        mockMvc.perform(get("/api/v1/decisions")
                .header("Authorization", "Bearer invalid-garbage-token"))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // Authenticated Requests & Role Consistency
    // ==========================================

    @Test
    @DisplayName("Authenticated USER with valid Bearer token successfully passes security filter")
    void shouldAllowAuthenticatedUserToPassSecurityFilter() throws Exception {
        String email = "regular.user@example.com";
        String token = jwtService.generateToken(email);

        User user = User.builder()
                .fullName("Regular User")
                .email(email)
                .password("$2a$10$hashedPass")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();
        user.setId(50L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Request successfully passes security filter (not 401 Unauthorized and not 403 Forbidden)
        mockMvc.perform(get("/api/v1/decisions")
                .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isNotIn(401, 403);
                });
    }

    @Test
    @DisplayName("Authenticated ADMIN with valid Bearer token successfully passes security filter")
    void shouldAllowAuthenticatedAdminToPassSecurityFilter() throws Exception {
        String email = "admin.user@example.com";
        String token = jwtService.generateToken(email);

        User adminUser = User.builder()
                .fullName("Admin User")
                .email(email)
                .password("$2a$10$hashedPass")
                .role(UserRole.ROLE_ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();
        adminUser.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        // Admin request successfully passes security filter (not 401 Unauthorized and not 403 Forbidden)
        mockMvc.perform(get("/api/v1/decisions")
                .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isNotIn(401, 403);
                });
    }

    @Test
    @DisplayName("Role enum names and Spring Security authority conventions are consistent")
    void shouldVerifyRoleNamesAndAuthorityConsistency() {
        assertThat(UserRole.ROLE_USER.name()).isEqualTo("ROLE_USER");
        assertThat(UserRole.ROLE_COMMUNITY_MODERATOR.name()).isEqualTo("ROLE_COMMUNITY_MODERATOR");
        assertThat(UserRole.ROLE_ADMIN.name()).isEqualTo("ROLE_ADMIN");

        // Verify all roles start with standard Spring Security ROLE_ prefix
        for (UserRole role : UserRole.values()) {
            assertThat(role.name()).startsWith("ROLE_");
        }
    }
}
