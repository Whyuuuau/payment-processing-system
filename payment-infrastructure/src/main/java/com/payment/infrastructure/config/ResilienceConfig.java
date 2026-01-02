package com.payment.infrastructure.config;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j configuration for fault tolerance
 * Implements circuit breakers, retries, rate limiting, and bulkheads
 */
@Configuration
public class ResilienceConfig {

    /**
     * Circuit Breaker Configuration
     * Opens circuit when failure rate exceeds threshold
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            // Sliding window of 100 calls to calculate failure rate
            .slidingWindowSize(100)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            
            // Open circuit when 50% of calls fail
            .failureRateThreshold(50.0f)
            
            // Minimum number of calls before calculating failure rate
            .minimumNumberOfCalls(10)
            
            // Wait 60 seconds in OPEN state before transitioning to HALF_OPEN
            .waitDurationInOpenState(Duration.ofSeconds(60))
            
            // Allow 10 calls in HALF_OPEN state to test if service recovered
            .permittedNumberOfCallsInHalfOpenState(10)
            
            // Automatically transition from OPEN to HALF_OPEN
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            
            // Slow call threshold - consider call failed if takes longer than 5 seconds
            .slowCallDurationThreshold(Duration.ofSeconds(5))
            .slowCallRateThreshold(50.0f)
            
            .build();
    }

    /**
     * Retry Configuration
     * Automatically retries failed calls with exponential backoff
     */
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
            // Maximum 3 retry attempts
            .maxAttempts(3)
            
            // Initial wait duration of 1 second
            .waitDuration(Duration.ofSeconds(1))
            
            // Exponential backoff multiplier (1s, 2s, 4s)
            .intervalFunction(retry -> Duration.ofSeconds(1).toMillis() * (1L << (retry - 1)))
            
            // Retry on specific exceptions
            .retryExceptions(
                java.util.concurrent.TimeoutException.class,
                java.io.IOException.class,
                org.springframework.dao.OptimisticLockingFailureException.class
            )
            
            .build();
    }

    /**
     * Rate Limiter Configuration
     * Limits number of requests per time period
     */
    @Bean
    public RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
            // Allow 1000 requests per second
            .limitForPeriod(1000)
            
            // Refresh period of 1 second
            .limitRefreshPeriod(Duration.ofSeconds(1))
            
            // Wait up to 5 seconds for permission
            .timeoutDuration(Duration.ofSeconds(5))
            
            .build();
    }

    /**
     * Bulkhead Configuration
     * Limits concurrent executions to prevent resource exhaustion
     */
    @Bean
    public BulkheadConfig bulkheadConfig() {
        return BulkheadConfig.custom()
            // Maximum 25 concurrent calls
            .maxConcurrentCalls(25)
            
            // Wait up to 100ms for permission
            .maxWaitDuration(Duration.ofMillis(100))
            
            .build();
    }
}
