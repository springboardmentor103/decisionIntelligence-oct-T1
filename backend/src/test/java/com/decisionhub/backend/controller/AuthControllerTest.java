package com.decisionhub.backend.controller;

import com.decisionhub.backend.dto.AuthResponse;
import com.decisionhub.backend.dto.LoginRequest;
import com.decisionhub.backend.dto.RegisterRequest;
import com.decisionhub.backend.exception.BadRequestException;
import com.decisionhub.backend.exception.ForbiddenException;
import com.decisionhub.backend.exception.UnauthorizedException;
import com.decisionhub.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    // ==========================================
    // Registration HTTP Tests
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/register with valid payload should return 201 Created and userId")
    void shouldReturn201WhenRegisteringValidUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Daniel Craig");
        request.setEmail("daniel@example.com");
        request.setPassword("SecurePass123!");

        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(42L);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(42));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email should return 400 Bad Request")
    void shouldReturn400WhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Duplicate User");
        request.setEmail("existing@example.com");
        request.setPassword("SecurePass123!");

        when(authService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new BadRequestException("Email is already registered"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid email format should return 400 Bad Request")
    void shouldReturn400WhenEmailFormatIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Invalid Email User");
        request.setEmail("not-a-valid-email");
        request.setPassword("SecurePass123!");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with blank full name should return 400 Bad Request")
    void shouldReturn400WhenFullNameIsBlank() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("   ");
        request.setEmail("valid@example.com");
        request.setPassword("SecurePass123!");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with password shorter than 8 chars should return 400 Bad Request")
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Short Pass User");
        request.setEmail("short@example.com");
        request.setPassword("short");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    // ==========================================
    // Login HTTP Tests
    // ==========================================

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials should return 200 OK and AuthResponse")
    void shouldReturn200AndAuthResponseOnSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("ValidPassword123!");

        AuthResponse authResponse = new AuthResponse(
                "mock.jwt.token",
                "Bearer",
                10L,
                "Regular User",
                "user@example.com",
                "ROLE_USER"
        );

        when(authService.loginUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.fullName").value("Regular User"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid password or unknown email should return 401 Unauthorized")
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("WrongPassword!");

        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with inactive or suspended account should return 403 Forbidden")
    void shouldReturn403WhenAccountIsSuspended() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("suspended@example.com");
        request.setPassword("ValidPassword123!");

        when(authService.loginUser(any(LoginRequest.class)))
                .thenThrow(new ForbiddenException("Account is not active"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Account is not active"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with blank fields should return 400 Bad Request")
    void shouldReturn400WhenLoginRequestIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
