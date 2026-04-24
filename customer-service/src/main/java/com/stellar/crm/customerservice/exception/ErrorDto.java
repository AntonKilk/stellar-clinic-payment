package com.stellar.crm.customerservice.exception;

import java.time.Instant;

public record ErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
