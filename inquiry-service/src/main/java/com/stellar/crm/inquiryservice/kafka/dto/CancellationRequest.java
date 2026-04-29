package com.stellar.crm.inquiryservice.kafka.dto;

import java.time.Instant;
import java.util.UUID;

public record CancellationRequest(
        UUID correlationId,
        UUID inquiryId,
        UUID groupRefId,
        Instant requestedAt
) {
}
