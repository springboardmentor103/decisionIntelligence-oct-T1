package com.decisionhub.backend.security;

import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valid Bearer token with ACTIVE user should populate SecurityContextHolder with correct role")
    void shouldAuthenticateValidBearerTokenWithActiveUser() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "john@example.com";

        User user = User.builder()
                .fullName("John Doe")
                .email(email)
                .password("encoded_pass")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(email);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly(UserRole.ROLE_USER.name());

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Valid Bearer token with ADMIN role should populate ROLE_ADMIN authority")
    void shouldAuthenticateAdminUserWithRoleAdminAuthority() throws ServletException, IOException {
        String token = "valid.admin.token";
        String email = "admin@example.com";

        User adminUser = User.builder()
                .fullName("Admin User")
                .email(email)
                .password("encoded_pass")
                .role(UserRole.ROLE_ADMIN)
                .status(AccountStatus.ACTIVE)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(adminUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly(UserRole.ROLE_ADMIN.name());

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Missing Authorization header should skip authentication and proceed down filter chain")
    void shouldSkipAuthenticationWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    @DisplayName("Non-Bearer Authorization header should skip authentication and proceed down filter chain")
    void shouldSkipAuthenticationWhenHeaderIsNotBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    @DisplayName("Invalid token should reject authentication and proceed down filter chain")
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        String token = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("Inactive or suspended account should NOT be authenticated even with valid token")
    void shouldNotAuthenticateWhenAccountIsSuspended() throws ServletException, IOException {
        String token = "valid.token";
        String email = "suspended@example.com";

        User suspendedUser = User.builder()
                .fullName("Suspended User")
                .email(email)
                .password("encoded_pass")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.SUSPENDED)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(suspendedUser));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Unknown email in token should NOT authenticate when user is not found in database")
    void shouldNotAuthenticateWhenUserNotFoundInDatabase() throws ServletException, IOException {
        String token = "valid.token";
        String email = "ghost@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

