package com.stellar.crm.paymentservice.client.xpayment;

import com.stellar.crm.paymentservice.client.xpayment.dto.ChargeResponse;
import com.stellar.crm.paymentservice.client.xpayment.dto.CreateChargeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class XPaymentClient {

    @Qualifier("xPaymentRestClient")
    private final RestClient restClient;

    public ChargeResponse createCharge(CreateChargeRequest request) {
        return restClient.post()
                .uri("/charges")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChargeResponse.class);
    }

    public ChargeResponse getCharge(UUID id) {
        return restClient.get()
                .uri("/charges/{id}", id)
                .retrieve()
                .body(ChargeResponse.class);
    }
}
