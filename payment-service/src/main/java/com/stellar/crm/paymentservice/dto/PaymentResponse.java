package com.stellar.crm.paymentservice.dto;

import com.stellar.crm.contracts.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID inquiryRefId,
        BigDecimal amount,
        String currency,
        UUID transactionRefId,
        PaymentStatus status,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
