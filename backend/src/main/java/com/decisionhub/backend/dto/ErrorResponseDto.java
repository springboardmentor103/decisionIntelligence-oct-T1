package com.decisionhub.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDto(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<String> details
) {
    public static ErrorResponseDto of(int status, String error, String message, String path, List<String> details) {
        return new ErrorResponseDto(status, error, message, path, LocalDateTime.now(), details);
    }
}
