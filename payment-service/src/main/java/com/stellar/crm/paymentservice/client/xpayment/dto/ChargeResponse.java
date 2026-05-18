package com.stellar.crm.paymentservice.client.xpayment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ChargeResponse(
        UUID id,
        BigDecimal amount,
        String currency,
        BigDecimal amountReceived,
        Instant createdAt,
        Instant chargedAt,
        String customer,
        UUID order,
        String receiptEmail,
        ChargeStatus status,
        Map<String, String> metadata
) {
}
