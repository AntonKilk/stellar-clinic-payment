package com.stellar.crm.customerservice.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        ContactDetailsResponse contactDetails,
        Instant updatedAt,
        Instant createdAt
) {
}
