package com.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.payment.common.enums.Currency;
import com.payment.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for payment operations
 * Returns comprehensive payment details to clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    /**
     * Unique payment transaction ID
     */
    private String paymentId;

    /**
     * Idempotency key provided in request
     */
    private String idempotencyKey;

    /**
     * Current payment status
     */
    private PaymentStatus status;

    /**
     * Payment amount
     */
    private BigDecimal amount;

    /**
     * Currency
     */
    private Currency currency;

    /**
     * Merchant ID
     */
    private String merchantId;

    /**
     * Customer ID
     */
    private String customerId;

    /**
     * Payment method used
     */
    private String paymentMethod;

    /**
     * Description/reference
     */
    private String description;

    /**
     * Additional metadata
     */
    private Map<String, Object> metadata;

    /**
     * Payment creation timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Completion timestamp (if completed)
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * Failure reason (if failed)
     */
    private String failureReason;

    /**
     * Version for optimistic locking
     */
    private Long version;
}
