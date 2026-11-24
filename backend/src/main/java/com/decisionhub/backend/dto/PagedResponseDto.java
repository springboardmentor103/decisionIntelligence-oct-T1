package com.decisionhub.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses.
 */
public record PagedResponseDto<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
) {
    public static <T> PagedResponseDto<T> from(Page<T> page) {
        return new PagedResponseDto<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
