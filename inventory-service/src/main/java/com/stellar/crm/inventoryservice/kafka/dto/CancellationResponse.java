package com.stellar.crm.inventoryservice.kafka.dto;

import java.util.UUID;

public record CancellationResponse(
        UUID correlationId,
        UUID inquiryId,
        UUID groupRefId,
        Status status,
        String message
) {
    public enum Status {
        SUCCESS,
        NOT_FOUND,
        ALREADY_RELEASED,
        ERROR
    }
}
