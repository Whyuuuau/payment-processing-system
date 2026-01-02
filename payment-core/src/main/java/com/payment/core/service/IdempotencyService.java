package com.payment.core.service;

import com.payment.common.exception.IdempotencyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Idempotency service using Redis distributed cache
 * Prevents duplicate payment processing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final RedissonClient redissonClient;
    
    private static final String IDEMPOTENCY_KEY_PREFIX = "payment:idempotency:";
    private static final long IDEMPOTENCY_TTL_HOURS = 24;

    /**
     * Check if idempotency key exists
     * Throws exception if duplicate detected
     * 
     * @param idempotencyKey Unique idempotency key from client
     * @param paymentId Associated payment ID
     * @throws IdempotencyException if key already exists
     */
    public void checkAndStore(String idempotencyKey, String paymentId) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        
        RMapCache<String, String> cache = redissonClient.getMapCache("idempotency");
        
        // Try to put if absent (atomic operation)
        String existingPaymentId = cache.putIfAbsent(
            redisKey,
            paymentId,
            IDEMPOTENCY_TTL_HOURS,
            TimeUnit.HOURS
        );
        
        if (existingPaymentId != null) {
            log.warn("Duplicate payment request detected. Idempotency key: {}, Existing payment: {}", 
                     idempotencyKey, existingPaymentId);
            throw new IdempotencyException(idempotencyKey);
        }
        
        log.debug("Idempotency key stored: {} -> {}", idempotencyKey, paymentId);
    }

    /**
     * Get payment ID for idempotency key
     * Returns null if not found
     */
    public String getPaymentId(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        RMapCache<String, String> cache = redissonClient.getMapCache("idempotency");
        return cache.get(redisKey);
    }

    /**
     * Remove idempotency key (for testing/cleanup)
     */
    public void remove(String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        RMapCache<String, String> cache = redissonClient.getMapCache("idempotency");
        cache.remove(redisKey);
    }
}
