package com.stellar.crm.inquiryservice.exception;

import java.time.Instant;

public record ErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
