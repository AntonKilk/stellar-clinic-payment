package com.stellar.crm.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaymentServiceApplication {

    private PaymentServiceApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
