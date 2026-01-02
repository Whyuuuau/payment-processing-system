package com.payment.persistence.entity;

import com.payment.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Payment Event entity for event sourcing
 * Stores immutable history of all payment state changes
 */
@Entity
@Table(name = "payment_events", indexes = {
    @Index(name = "idx_payment_event_payment_id", columnList = "payment_id"),
    @Index(name = "idx_payment_event_timestamp", columnList = "event_timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", length = 36)
    private String eventId;

    /**
     * Associated payment ID
     */
    @Column(name = "payment_id", nullable = false, length = 36)
    private String paymentId;

    /**
     * Event type (e.g., CREATED, STATUS_CHANGED, REFUNDED)
     */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    /**
     * Previous status (null for creation)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private PaymentStatus previousStatus;

    /**
     * New status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private PaymentStatus newStatus;

    /**
     * Event metadata
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private Map<String, Object> eventData;

    /**
     * Event timestamp (immutable)
     */
    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp;

    @PrePersist
    protected void onCreate() {
        this.eventTimestamp = LocalDateTime.now();
    }
}
