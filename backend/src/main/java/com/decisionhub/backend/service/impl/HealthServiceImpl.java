package com.decisionhub.backend.service.impl;

import com.decisionhub.backend.dto.HealthResponseDto;
import com.decisionhub.backend.service.HealthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HealthServiceImpl implements HealthService {

    @Value("${spring.application.name:backend}")
    private String applicationName;

    @Override
    public HealthResponseDto getHealthStatus() {
        Map<String, Object> details = new HashMap<>();
        details.put("database", "MySQL");
        details.put("environment", "Development");

        return HealthResponseDto.up(applicationName, "1.0.0", details);
    }
}
