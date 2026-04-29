package com.stellar.crm.inventoryservice.kafka.dto;

import java.time.Instant;
import java.util.UUID;

public record CancellationRequest(
        UUID correlationId,
        UUID inquiryId,
        UUID groupRefId,
        Instant requestedAt
) {
}
