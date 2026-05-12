package com.stellar.crm.paymentservice.kafka.dto;

import com.stellar.crm.contracts.payment.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentStatusUpdated(
        UUID paymentId,
        UUID inquiryRefId,
        PaymentStatus status,
        Instant updatedAt
) {
}
