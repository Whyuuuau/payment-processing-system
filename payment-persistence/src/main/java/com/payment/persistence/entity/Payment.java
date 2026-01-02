package com.payment.persistence.entity;

import com.payment.common.enums.Currency;
import com.payment.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment entity with optimistic locking
 * Stores payment transaction data with comprehensive indexing for performance
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_merchant_created", columnList = "merchant_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", length = 36)
    private String paymentId;

    /**
     * Idempotency key - unique constraint prevents duplicate processing
     */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * Amount with precision for financial calculations
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    /**
     * Merchant/seller identifier
     */
    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;

    /**
     * Customer/buyer identifier
     */
    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    /**
     * Payment method used
     */
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    /**
     * Optional description
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Additional metadata stored as JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Failure reason if payment failed
     */
    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Completion timestamp
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Version for optimistic locking
     * Hibernate automatically increments this on each update
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Automatically set timestamps before persist
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    /**
     * Automatically update timestamp before update
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status != null && this.status.isTerminal() && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
}
