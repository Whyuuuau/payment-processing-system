package com.payment.api.exception;

import com.payment.common.exception.IdempotencyException;
import com.payment.common.exception.PaymentException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Global exception handler for REST API
 * Provides consistent error responses
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle payment exceptions
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
        log.error("Payment exception: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ex.getHttpStatus())
            .error(HttpStatus.valueOf(ex.getHttpStatus()).getReasonPhrase())
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    /**
     * Handle idempotency exceptions
     */
    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyException(IdempotencyException ex) {
        log.warn("Idempotency exception: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ex.getHttpStatus())
            .error("Conflict")
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle optimistic locking failures
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(OptimisticLockingFailureException ex) {
        log.error("Optimistic locking failure: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message("The payment was modified by another request. Please retry.")
            .errorCode("CONCURRENT_MODIFICATION")
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle circuit breaker open
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCallNotPermitted(CallNotPermittedException ex) {
        log.error("Circuit breaker is open: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error("Service Unavailable")
            .message("Service is temporarily unavailable. Please try again later.")
            .errorCode("CIRCUIT_BREAKER_OPEN")
            .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Handle rate limiting
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRequestNotPermitted(RequestNotPermitted ex) {
        log.warn("Rate limit exceeded");
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.TOO_MANY_REQUESTS.value())
            .error("Too Many Requests")
            .message("Rate limit exceeded. Please try again later.")
            .errorCode("RATE_LIMIT_EXCEEDED")
            .build();
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    /**
     * Handle timeout exceptions
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(TimeoutException ex) {
        log.error("Request timeout: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.REQUEST_TIMEOUT.value())
            .error("Request Timeout")
            .message("Request processing timed out. Please try again.")
            .errorCode("REQUEST_TIMEOUT")
            .build();
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message("Validation failed")
            .errorCode("VALIDATION_ERROR")
            .validationErrors(validationErrors)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred. Please contact support.")
            .errorCode("INTERNAL_ERROR")
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String errorCode;
        private Map<String, String> validationErrors;
    }
}
