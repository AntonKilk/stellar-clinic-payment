package com.stellar.crm.paymentservice.exception;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> details,
        UUID resourceId,
        String path
) {
}
