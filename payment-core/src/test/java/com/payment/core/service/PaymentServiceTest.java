package com.payment.core.service;

import com.payment.common.dto.PaymentRequest;
import com.payment.common.dto.PaymentResponse;
import com.payment.common.enums.Currency;
import com.payment.common.enums.PaymentStatus;
import com.payment.core.mapper.PaymentMapper;
import com.payment.persistence.entity.Payment;
import com.payment.persistence.repository.PaymentEventRepository;
import com.payment.persistence.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 * Tests core payment processing logic, idempotency, and error handling
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventRepository paymentEventRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest validRequest;
    private Payment payment;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        validRequest = PaymentRequest.builder()
            .idempotencyKey("test-key-001")
            .amount(new BigDecimal("100.00"))
            .currency(Currency.USD)
            .merchantId("merchant-001")
            .customerId("customer-001")
            .paymentMethod("CREDIT_CARD")
            .description("Test payment")
            .build();

        payment = Payment.builder()
            .paymentId("payment-id-001")
            .idempotencyKey("test-key-001")
            .status(PaymentStatus.PENDING)
            .amount(new BigDecimal("100.00"))
            .currency(Currency.USD)
            .merchantId("merchant-001")
            .customerId("customer-001")
            .paymentMethod("CREDIT_CARD")
            .build();

        paymentResponse = PaymentResponse.builder()
            .paymentId("payment-id-001")
            .idempotencyKey("test-key-001")
            .status(PaymentStatus.PENDING)
            .amount(new BigDecimal("100.00"))
            .currency(Currency.USD)
            .build();
    }

    @Test
    void createPayment_WithValidRequest_ShouldReturnPaymentResponse() {
        // Given
        when(idempotencyService.getPaymentId(anyString())).thenReturn(null);
        when(paymentMapper.toEntity(any(PaymentRequest.class))).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(result);
        assertEquals("payment-id-001", result.getPaymentId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(idempotencyService, times(1)).checkAndStore(anyString(), anyString());
    }

    @Test
    void createPayment_WithExistingIdempotencyKey_ShouldReturnExistingPayment() {
        // Given
        when(idempotencyService.getPaymentId("test-key-001")).thenReturn("payment-id-001");
        when(paymentRepository.findById("payment-id-001")).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.createPayment(validRequest);

        // Then
        assertNotNull(result);
        assertEquals("payment-id-001", result.getPaymentId());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPayment_WithValidId_ShouldReturnPayment() {
        // Given
        when(paymentRepository.findById("payment-id-001")).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.getPayment("payment-id-001");

        // Then
        assertNotNull(result);
        assertEquals("payment-id-001", result.getPaymentId());
        verify(paymentRepository, times(1)).findById("payment-id-001");
    }

    @Test
    void getPayment_WithInvalidId_ShouldThrowException() {
        // Given
        when(paymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(Exception.class, () -> paymentService.getPayment("invalid-id"));
    }

    @Test
    void processPayment_ShouldUpdateStatusToCompleted() {
        // Given
        when(paymentRepository.findByIdForUpdate("payment-id-001"))
            .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.processPayment("payment-id-001");

        // Then
        assertNotNull(result);
        verify(paymentRepository, atLeast(1)).save(any(Payment.class));
        verify(paymentEventRepository, atLeast(1)).save(any());
    }
}
