package com.stellar.crm.paymentservice.client.xpayment.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CreateChargeRequest(
        BigDecimal amount,
        String currency,
        String customer,
        UUID order,
        String receiptEmail,
        Map<String, String> metadata
) {
}
