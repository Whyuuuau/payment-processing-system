package com.payment.persistence.repository;

import com.payment.persistence.entity.PaymentEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Payment Event entity
 * Stores immutable event history
 */
@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, String> {

    /**
     * Find all events for a payment
     */
    List<PaymentEvent> findByPaymentIdOrderByEventTimestampAsc(String paymentId);

    /**
     * Find events within date range
     */
    Page<PaymentEvent> findByEventTimestampBetween(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Find events for a payment with pagination
     */
    Page<PaymentEvent> findByPaymentId(String paymentId, Pageable pageable);
}
