package com.stellar.crm.customerservice.dto;

import java.time.Instant;
import java.util.UUID;

public record ContactDetailsResponse(
        UUID id,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
}
