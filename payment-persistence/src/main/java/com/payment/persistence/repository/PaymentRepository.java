package com.payment.persistence.repository;

import com.payment.common.enums.PaymentStatus;
import com.payment.persistence.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Payment entity
 * Includes custom queries with pessimistic locking for concurrent updates
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Find payment by idempotency key
     * Used to prevent duplicate processing
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find payment by idempotency key with pessimistic write lock
     * Prevents concurrent updates to the same payment
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.idempotencyKey = :idempotencyKey")
    Optional<Payment> findByIdempotencyKeyForUpdate(@Param("idempotencyKey") String idempotencyKey);

    /**
     * Find payment by ID with pessimistic write lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByIdForUpdate(@Param("paymentId") String paymentId);

    /**
     * Find payments by merchant ID with pagination
     */
    Page<Payment> findByMerchantId(String merchantId, Pageable pageable);

    /**
     * Find payments by customer ID with pagination
     */
    Page<Payment> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find payments by status
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Find payments by merchant and status
     */
    Page<Payment> findByMerchantIdAndStatus(String merchantId, PaymentStatus status, Pageable pageable);

    /**
     * Find payments by merchant within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId " +
           "AND p.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY p.createdAt DESC")
    Page<Payment> findByMerchantAndDateRange(
        @Param("merchantId") String merchantId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Count payments by status for a merchant
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.status = :status")
    long countByMerchantAndStatus(@Param("merchantId") String merchantId, @Param("status") PaymentStatus status);

    /**
     * Check if idempotency key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
}
