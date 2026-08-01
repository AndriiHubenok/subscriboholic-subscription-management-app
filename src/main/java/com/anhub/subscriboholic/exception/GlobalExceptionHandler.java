package com.anhub.subscriboholic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSubscriptionNotFound(SubscriptionNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedSubscriptionAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedAccess(UnauthorizedSubscriptionAccessException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Forbidden");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}
