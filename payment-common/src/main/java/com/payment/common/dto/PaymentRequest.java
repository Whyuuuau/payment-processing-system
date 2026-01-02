package com.payment.common.dto;

import com.payment.common.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Request DTO for creating a payment
 * Includes idempotency key to prevent duplicate transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    /**
     * Unique idempotency key to prevent duplicate processing
     * Client should generate this (e.g., UUID) and reuse for retries
     */
    @NotBlank(message = "Idempotency key is required")
    @Size(min = 1, max = 255, message = "Idempotency key must be between 1 and 255 characters")
    private String idempotencyKey;

    /**
     * Payment amount - must be positive
     */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have max 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    /**
     * Currency code
     */
    @NotNull(message = "Currency is required")
    private Currency currency;

    /**
     * Merchant/seller ID
     */
    @NotBlank(message = "Merchant ID is required")
    @Size(max = 100, message = "Merchant ID must not exceed 100 characters")
    private String merchantId;

    /**
     * Customer ID
     */
    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID must not exceed 100 characters")
    private String customerId;

    /**
     * Payment method (e.g., CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER)
     */
    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    /**
     * Optional description/reference
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Additional metadata (stored as JSON)
     */
    private Map<String, Object> metadata;
}
