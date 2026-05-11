package com.stellar.crm.inquiryservice.kafka.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentStatusUpdated(
        UUID paymentId,
        UUID inquiryRefId,
        PaymentStatus status,
        Instant updatedAt
) {
    public enum PaymentStatus {
        RECEIVED,
        PENDING,
        DECLINED,
        APPROVED,
        NOT_SENT
    }
}

