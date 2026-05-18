package com.stellar.crm.paymentservice.client.xpayment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ChargeStatus {
    @JsonProperty("processing") PROCESSING,
    @JsonProperty("succeeded")  SUCCEEDED,
    @JsonProperty("canceled")   CANCELED
}
