package com.stellar.crm.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID inquiryRefId,
        BigDecimal amount,
        String currency
) {
}
