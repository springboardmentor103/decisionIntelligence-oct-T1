package com.decisionhub.backend.exception;

import com.decisionhub.backend.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.of(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            List.of()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.of(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            List.of()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        ErrorResponseDto error = ErrorResponseDto.of(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Request validation failed with errors",
            request.getRequestURI(),
            details
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, HttpServletRequest request) {
        ErrorResponseDto error = ErrorResponseDto.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            List.of()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
