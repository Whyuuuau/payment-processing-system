package com.payment.core.service;

import com.payment.common.dto.PaymentRequest;
import com.payment.common.dto.PaymentResponse;
import com.payment.common.enums.PaymentStatus;
import com.payment.common.exception.PaymentException;
import com.payment.core.mapper.PaymentMapper;
import com.payment.persistence.entity.Payment;
import com.payment.persistence.entity.PaymentEvent;
import com.payment.persistence.repository.PaymentEventRepository;
import com.payment.persistence.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Core payment processing service
 * Implements idempotency, optimistic locking, and event publishing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final IdempotencyService idempotencyService;
    private final PaymentMapper paymentMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-events";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Create a new payment
     * Uses SERIALIZABLE isolation for critical financial transactions
     * 
     * @param request Payment request with idempotency key
     * @return Payment response
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for merchant: {}, amount: {}, idempotency key: {}", 
                 request.getMerchantId(), request.getAmount(), request.getIdempotencyKey());

        // Check idempotency first (in Redis)
        String existingPaymentId = idempotencyService.getPaymentId(request.getIdempotencyKey());
        if (existingPaymentId != null) {
            log.info("Payment already exists for idempotency key: {}", request.getIdempotencyKey());
            Payment existing = paymentRepository.findById(existingPaymentId)
                .orElseThrow(() -> new PaymentException(
                    "Payment not found: " + existingPaymentId,
                    "PAYMENT_NOT_FOUND",
                    404
                ));
            return paymentMapper.toResponse(existing);
        }

        // Create payment entity
        Payment payment = paymentMapper.toEntity(request);
        payment.setStatus(PaymentStatus.PENDING);

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Store idempotency key
        idempotencyService.checkAndStore(request.getIdempotencyKey(), savedPayment.getPaymentId());

        // Create event
        createPaymentEvent(savedPayment.getPaymentId(), "PAYMENT_CREATED", null, PaymentStatus.PENDING);

        // Publish to Kafka for async processing
        publishPaymentEvent(savedPayment, "PAYMENT_CREATED");

        log.info("Payment created successfully: {}", savedPayment.getPaymentId());
        return paymentMapper.toResponse(savedPayment);
    }

    /**
     * Process payment (typically called asynchronously)
     * Implements retry logic for optimistic locking failures
     */
    @Transactional
    public PaymentResponse processPayment(String paymentId) {
        log.info("Processing payment: {}", paymentId);

        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                // Lock payment for update
                Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                    .orElseThrow(() -> new PaymentException(
                        "Payment not found: " + paymentId,
                        "PAYMENT_NOT_FOUND",
                        404
                    ));

                // Check if already processed
                if (payment.getStatus().isTerminal()) {
                    log.info("Payment already in terminal state: {}", payment.getStatus());
                    return paymentMapper.toResponse(payment);
                }

                // Update status to PROCESSING
                PaymentStatus previousStatus = payment.getStatus();
                payment.setStatus(PaymentStatus.PROCESSING);
                payment = paymentRepository.save(payment);

                createPaymentEvent(paymentId, "STATUS_CHANGED", previousStatus, PaymentStatus.PROCESSING);

                // Simulate payment processing (replace with actual payment gateway integration)
                boolean success = simulatePaymentProcessing(payment);

                // Update final status
                previousStatus = payment.getStatus();
                if (success) {
                    payment.setStatus(PaymentStatus.COMPLETED);
                    log.info("Payment completed successfully: {}", paymentId);
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setFailureReason("Payment processing failed");
                    log.error("Payment processing failed: {}", paymentId);
                }

                payment = paymentRepository.save(payment);
                createPaymentEvent(paymentId, "STATUS_CHANGED", previousStatus, payment.getStatus());

                // Publish completion event
                publishPaymentEvent(payment, success ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED");

                return paymentMapper.toResponse(payment);

            } catch (OptimisticLockingFailureException e) {
                attempts++;
                log.warn("Optimistic locking failure, attempt {}/{}", attempts, MAX_RETRY_ATTEMPTS);
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    throw new PaymentException(
                        "Failed to update payment after " + MAX_RETRY_ATTEMPTS + " attempts",
                        "CONCURRENT_UPDATE_FAILURE",
                        409,
                        e
                    );
                }
                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep((long) Math.pow(2, attempts) * 100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new PaymentException("Interrupted during retry", "INTERRUPTED", 500, ie);
                }
            }
        }

        throw new PaymentException("Unexpected error in payment processing", "UNEXPECTED_ERROR", 500);
    }

    /**
     * Get payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new PaymentException(
                "Payment not found: " + paymentId,
                "PAYMENT_NOT_FOUND",
                404
            ));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Get payments by merchant
     */
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByMerchant(String merchantId, Pageable pageable) {
        return paymentRepository.findByMerchantId(merchantId, pageable)
            .map(paymentMapper::toResponse);
    }

    /**
     * Refund payment
     */
    @Transactional
    public PaymentResponse refundPayment(String paymentId) {
        log.info("Refunding payment: {}", paymentId);

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(() -> new PaymentException(
                "Payment not found: " + paymentId,
                "PAYMENT_NOT_FOUND",
                404
            ));

        // Validate can refund
        if (!payment.getStatus().canRefund()) {
            throw new PaymentException(
                "Payment cannot be refunded in status: " + payment.getStatus(),
                "INVALID_STATUS_FOR_REFUND",
                400
            );
        }

        PaymentStatus previousStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        createPaymentEvent(paymentId, "PAYMENT_REFUNDED", previousStatus, PaymentStatus.REFUNDED);
        publishPaymentEvent(payment, "PAYMENT_REFUNDED");

        log.info("Payment refunded successfully: {}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Create payment event for audit trail
     */
    private void createPaymentEvent(String paymentId, String eventType, 
                                   PaymentStatus previousStatus, PaymentStatus newStatus) {
        PaymentEvent event = PaymentEvent.builder()
            .paymentId(paymentId)
            .eventType(eventType)
            .previousStatus(previousStatus)
            .newStatus(newStatus)
            .eventData(new HashMap<>())
            .build();
        
        paymentEventRepository.save(event);
    }

    /**
     * Publish payment event to Kafka
     */
    private void publishPaymentEvent(Payment payment, String eventType) {
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("paymentId", payment.getPaymentId());
            eventData.put("merchantId", payment.getMerchantId());
            eventData.put("customerId", payment.getCustomerId());
            eventData.put("amount", payment.getAmount());
            eventData.put("currency", payment.getCurrency());
            eventData.put("status", payment.getStatus());
            eventData.put("eventType", eventType);
            eventData.put("timestamp", System.currentTimeMillis());

            kafkaTemplate.send(PAYMENT_TOPIC, payment.getPaymentId(), eventData);
            log.debug("Published event to Kafka: {}", eventType);
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
            // Don't fail the transaction due to Kafka failure
        }
    }

    /**
     * Simulate payment processing (replace with actual gateway integration)
     */
    private boolean simulatePaymentProcessing(Payment payment) {
        try {
            // Simulate processing time
            Thread.sleep(100);
            // Simulate 95% success rate
            return Math.random() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
