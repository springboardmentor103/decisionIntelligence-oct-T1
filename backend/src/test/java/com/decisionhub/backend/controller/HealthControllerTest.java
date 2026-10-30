package com.decisionhub.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/health should return 200 OK and UP status")
    void shouldReturnHealthStatusUpForApiV1() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.application").value("backend"));
    }

    @Test
    @DisplayName("GET /api/health should return 200 OK and UP status")
    void shouldReturnHealthStatusUpForApiHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.application").value("backend"));
    }

    @Test
    @DisplayName("GET / should return 200 OK and UP status without authentication prompt")
    void shouldReturnHealthStatusUpForRootPath() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.application").value("backend"));
    }

    @Test
    @DisplayName("Protected endpoints should return 401 Unauthorized when unauthenticated")
    void shouldDenyUnauthenticatedAccessToProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/decisions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CORS preflight request should succeed for configured origin")
    void shouldAllowCorsPreflightForConfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/health")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
