package com.axis.hraiportal.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // handles RuntimeException from service layer
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(
            RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // handles Supabase errors
    @ExceptionHandler(SupabaseException.class)
    public ResponseEntity<Map<String, String>> handleSupabase(
            SupabaseException ex) {
        log.error("Supabase error [{}]: {}",
                ex.getStatus(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "Database error",
                        "detail", ex.getMessage()));
    }

    // handles generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(
            Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Something went wrong"));
    }
    // handling validation for null title in session creation
    @ExceptionHandler(WebExchangeBindException.class)
    public Map<String, String> handleValidationException(
            WebExchangeBindException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getFieldErrors().forEach(error -> {
            errors.put(
                    error.getField(),
                    error.getDefaultMessage());
        });

        return errors;
    }
}
