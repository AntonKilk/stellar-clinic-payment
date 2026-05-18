package com.stellar.crm.paymentservice.client.xpayment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class XPaymentClientConfig {

    public static final String ACCOUNT_HEADER = "X-Pay-Account";

    @Bean
    public RestClient xPaymentRestClient(XPaymentProperties properties) {
        final String credentials = properties.username() + ":" + properties.password();
        final String basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth)
                .defaultHeader(ACCOUNT_HEADER, properties.account())
                .build();
    }
}
