package com.payment.api.controller;

import com.payment.common.dto.PaymentRequest;
import com.payment.common.dto.PaymentResponse;
import com.payment.core.service.PaymentService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for payment operations
 * Includes resilience patterns: circuit breaker, retry, rate limiter, bulkhead
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a new payment
     * 
     * @param request Payment request with idempotency key
     * @return Payment response
     */
    @PostMapping
    @CircuitBreaker(name = "paymentService", fallbackMethod = "createPaymentFallback")
    @RateLimiter(name = "paymentApi")
    @Bulkhead(name = "paymentBulkhead", type = Bulkhead.Type.THREADPOOL)
    @Operation(
        summary = "Create new payment",
        description = "Creates a new payment transaction with idempotency protection"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Duplicate idempotency key"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
        @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for merchant: {}", request.getMerchantId());
        
        PaymentResponse response = paymentService.createPayment(request);
        
        // Trigger async processing
        processPaymentAsync(response.getPaymentId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{id}")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "getPaymentFallback")
    @RateLimiter(name = "paymentApi")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by payment ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment found"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPayment(
        @Parameter(description = "Payment ID") @PathVariable String id) {
        log.info("Retrieving payment: {}", id);
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by merchant with pagination
     */
    @GetMapping
    @CircuitBreaker(name = "paymentService")
    @RateLimiter(name = "paymentApi")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByMerchant(
            @RequestParam String merchantId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Retrieving payments for merchant: {}", merchantId);
        Page<PaymentResponse> payments = paymentService.getPaymentsByMerchant(merchantId, pageable);
        return ResponseEntity.ok(payments);
    }

    /**
     * Refund a payment
     */
    @PostMapping("/{id}/refund")
    @CircuitBreaker(name = "paymentService", fallbackMethod = "refundPaymentFallback")
    @RateLimiter(name = "paymentApi")
    @Retry(name = "paymentRetry")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String id) {
        log.info("Refunding payment: {}", id);
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "payment-api");
        return ResponseEntity.ok(health);
    }

    /**
     * Async payment processing
     * Uses virtual threads executor
     */
    @Async("paymentTaskExecutor")
    public CompletableFuture<Void> processPaymentAsync(String paymentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                paymentService.processPayment(paymentId);
            } catch (Exception e) {
                log.error("Async payment processing failed for: {}", paymentId, e);
            }
        });
    }

    // ========== Fallback Methods ==========

    /**
     * Fallback for createPayment when circuit is open
     */
    public ResponseEntity<PaymentResponse> createPaymentFallback(PaymentRequest request, Exception e) {
        log.error("Circuit breaker activated for createPayment", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(PaymentResponse.builder()
                .failureReason("Service temporarily unavailable. Please try again later.")
                .build());
    }

    /**
     * Fallback for getPayment
     */
    public ResponseEntity<PaymentResponse> getPaymentFallback(String id, Exception e) {
        log.error("Circuit breaker activated for getPayment: {}", id, e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(PaymentResponse.builder()
                .paymentId(id)
                .failureReason("Service temporarily unavailable.")
                .build());
    }

    /**
     * Fallback for refundPayment
     */
    public ResponseEntity<PaymentResponse> refundPaymentFallback(String id, Exception e) {
        log.error("Circuit breaker activated for refundPayment: {}", id, e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(PaymentResponse.builder()
                .paymentId(id)
                .failureReason("Refund service temporarily unavailable.")
                .build());
    }
}
