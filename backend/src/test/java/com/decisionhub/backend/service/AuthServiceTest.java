package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.AuthResponse;
import com.decisionhub.backend.dto.LoginRequest;
import com.decisionhub.backend.dto.RegisterRequest;
import com.decisionhub.backend.entity.AccountStatus;
import com.decisionhub.backend.entity.User;
import com.decisionhub.backend.entity.UserRole;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.repository.UserRepository;
import com.decisionhub.backend.security.JwtService;
import com.decisionhub.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ==========================================
    // Registration Tests
    // ==========================================

    @Test
    @DisplayName("Should successfully register user with encoded password and default ROLE_USER and ACTIVE status")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Alice Wonderland");
        request.setEmail("Alice@Example.COM ");
        request.setPassword("PlainPassword123!");

        String encodedHash = "$2a$10$encodedPasswordHashMock";
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("PlainPassword123!")).thenReturn(encodedHash);

        User savedUser = User.builder()
                .fullName("Alice Wonderland")
                .email("alice@example.com")
                .password(encodedHash)
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();
        savedUser.setId(101L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Long userId = authService.registerUser(request);

        assertThat(userId).isEqualTo(101L);

        // Verify password is stored as encoded hash, NOT plaintext
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(capturedUser.getFullName()).isEqualTo("Alice Wonderland");
        assertThat(capturedUser.getPassword()).isEqualTo(encodedHash);
        assertThat(capturedUser.getPassword()).isNotEqualTo("PlainPassword123!");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.ROLE_USER);
        assertThat(capturedUser.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should reject registration if email is already registered")
    void shouldRejectDuplicateEmailRegistration() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Bob Builder");
        request.setEmail("bob@example.com");
        request.setPassword("Password88!");

        when(userRepository.existsByEmail("bob@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any());
    }

    // ==========================================
    // Login Tests
    // ==========================================

    @Test
    @DisplayName("Should successfully log in with valid credentials and return AuthResponse with JWT")
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("User@Domain.Com ");
        request.setPassword("SecretPassword123");

        User activeUser = User.builder()
                .fullName("Test User")
                .email("user@domain.com")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();
        activeUser.setId(202L);

        when(userRepository.findByEmail("user@domain.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("SecretPassword123", "$2a$10$hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("user@domain.com")).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.loginUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked.jwt.token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(202L);
        assertThat(response.getFullName()).isEqualTo("Test User");
        assertThat(response.getEmail()).isEqualTo("user@domain.com");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Should reject login when password does not match")
    void shouldRejectLoginWithIncorrectPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@domain.com");
        request.setPassword("WrongPassword!");

        User activeUser = User.builder()
                .fullName("Test User")
                .email("user@domain.com")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("user@domain.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongPassword!", "$2a$10$hashedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.loginUser(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Should reject login when email is not found in database")
    void shouldRejectLoginForUnknownEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@domain.com");
        request.setPassword("AnyPassword123");

        when(userRepository.findByEmail("unknown@domain.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginUser(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Should reject login when user account is SUSPENDED")
    void shouldRejectLoginForSuspendedAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail("suspended@domain.com");
        request.setPassword("CorrectPassword123");

        User suspendedUser = User.builder()
                .fullName("Suspended User")
                .email("suspended@domain.com")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.SUSPENDED)
                .build();

        when(userRepository.findByEmail("suspended@domain.com")).thenReturn(Optional.of(suspendedUser));
        when(passwordEncoder.matches("CorrectPassword123", "$2a$10$hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.loginUser(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Account is not active");

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Should reject login when user account is DEACTIVATED")
    void shouldRejectLoginForDeactivatedAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail("deactivated@domain.com");
        request.setPassword("CorrectPassword123");

        User deactivatedUser = User.builder()
                .fullName("Deactivated User")
                .email("deactivated@domain.com")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_USER)
                .status(AccountStatus.DEACTIVATED)
                .build();

        when(userRepository.findByEmail("deactivated@domain.com")).thenReturn(Optional.of(deactivatedUser));
        when(passwordEncoder.matches("CorrectPassword123", "$2a$10$hashedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.loginUser(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Account is not active");

        verifyNoInteractions(jwtService);
    }
}
