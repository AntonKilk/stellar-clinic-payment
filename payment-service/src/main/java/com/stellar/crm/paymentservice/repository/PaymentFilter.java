package com.stellar.crm.paymentservice.repository;
import com.stellar.crm.contracts.payment.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentFilter(
        UUID id,
        UUID inquiryRefId,
        PaymentStatus status,
        Instant createdAfter,
        Instant createdBefore
) { }


