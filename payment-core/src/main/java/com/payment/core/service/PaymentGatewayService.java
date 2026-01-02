package com.payment.core.service;

import com.payment.persistence.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Simulated payment gateway integration
 * 
 * In real world, this would call Stripe/Adyen/PayPal APIs
 * For now just randomly succeed/fail for testing purposes
 */
@Slf4j
@Service
public class PaymentGatewayService {

    private final Random random = new Random();

    /**
     * Simulate payment processing with external gateway
     * 
     * TODO: replace with actual payment gateway integration
     * Current options to consider:
     * - Stripe (easiest to integrate, good docs)
     * - Adyen (better for enterprise but complex)
     * - PayPal (meh, outdated API)
     * 
     * @return true if payment succeeded, false otherwise
     */
    public boolean processPaymentWithGateway(Payment payment) {
        log.info("Simulating payment gateway call for payment: {}", payment.getPaymentId());

        // simulate network delay
        try {
            Thread.sleep(100 + random.nextInt(400)); // 100-500ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        // 90% success rate for demo purposes
        // in production this would be actual gateway call
        boolean success = random.nextDouble() < 0.9;

        if (success) {
            log.info("Payment gateway approved payment: {}", payment.getPaymentId());
        } else {
            log.warn("Payment gateway declined payment: {}. Reason: insufficient funds (simulated)", 
                payment.getPaymentId());
        }

        return success;
    }

    /**
     * Check payment status with gateway
     * Not implemented yet - need this for reconciliation
     */
    public String checkPaymentStatus(String gatewayTransactionId) {
        // TODO: implement status check
        throw new UnsupportedOperationException("Status check not implemented");
    }

    /**
     * Process refund with gateway
     * Probably broken, hasn't been tested much
     */
    public boolean processRefund(Payment payment, String reason) {
        log.info("Processing refund for payment: {}, reason: {}", payment.getPaymentId(), reason);
        
        // HACK: just return true for now
        // actual refund would need to call gateway API
        // might fail if original payment wasn't settled yet
        return true;
    }
}
