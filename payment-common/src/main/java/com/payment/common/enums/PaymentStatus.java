package com.payment.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Payment lifecycle status
 * Represents the current state of a payment transaction
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("Payment initiated, awaiting processing"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment successfully completed"),
    FAILED("Payment processing failed"),
    REFUNDED("Payment has been refunded"),
    CANCELLED("Payment was cancelled");

    private final String description;

    /**
     * Check if the payment is in a terminal state
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED || this == CANCELLED;
    }

    /**
     * Check if the payment can be refunded
     */
    public boolean canRefund() {
        return this == COMPLETED;
    }
}
