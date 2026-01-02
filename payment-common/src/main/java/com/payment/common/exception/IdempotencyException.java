package com.payment.common.exception;

/**
 * Exception thrown when a duplicate payment request is detected
 * via idempotency key matching
 */
public class IdempotencyException extends PaymentException {

    public IdempotencyException(String idempotencyKey) {
        super(
                "Duplicate payment request detected with idempotency key: " + idempotencyKey,
                "DUPLICATE_REQUEST",
                409 // Conflict
        );
    }
}
