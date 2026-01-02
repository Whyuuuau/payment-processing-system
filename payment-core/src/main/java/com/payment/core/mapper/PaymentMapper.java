package com.payment.core.mapper;

import com.payment.common.dto.PaymentRequest;
import com.payment.common.dto.PaymentResponse;
import com.payment.persistence.entity.Payment;
import org.mapstruct.*;

/**
 * MapStruct mapper for Payment entity and DTOs
 * Automatically generates implementation at compile time
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {

    /**
     * Convert PaymentRequest DTO to Payment entity
     */
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    Payment toEntity(PaymentRequest request);

    /**
     * Convert Payment entity to PaymentResponse DTO
     */
    PaymentResponse toResponse(Payment payment);
}
