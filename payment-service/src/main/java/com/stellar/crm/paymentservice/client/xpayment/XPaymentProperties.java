package com.stellar.crm.paymentservice.client.xpayment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "provider.xpayment")
public record XPaymentProperties(
        String baseUrl,
        String username,
        String password,
        String account
) {
}
