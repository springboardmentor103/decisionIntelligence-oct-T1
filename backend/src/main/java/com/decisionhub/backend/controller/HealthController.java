package com.decisionhub.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decisionhub.backend.dto.ApiResponseDto;
import com.decisionhub.backend.dto.HealthResponseDto;
import com.decisionhub.backend.service.HealthService;

@RestController
@RequestMapping
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping({"/", "/api/v1/health", "/api/health"})
    public ResponseEntity<ApiResponseDto<HealthResponseDto>> checkHealth() {
        HealthResponseDto health = healthService.getHealthStatus();
        return ResponseEntity.ok(ApiResponseDto.success("Service is healthy and running", health));
    }
}
