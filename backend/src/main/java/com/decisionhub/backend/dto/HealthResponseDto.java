package com.decisionhub.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record HealthResponseDto(
    String status,
    String application,
    String version,
    LocalDateTime timestamp,
    Map<String, Object> details
) {
    public static HealthResponseDto up(String application, String version, Map<String, Object> details) {
        return new HealthResponseDto("UP", application, version, LocalDateTime.now(), details);
    }
}
