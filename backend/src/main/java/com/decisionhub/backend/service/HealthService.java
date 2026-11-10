package com.decisionhub.backend.service;

import com.decisionhub.backend.dto.HealthResponseDto;

public interface HealthService {

    HealthResponseDto getHealthStatus();
}
