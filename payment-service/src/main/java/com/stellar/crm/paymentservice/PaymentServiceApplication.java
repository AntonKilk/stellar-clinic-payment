package com.stellar.crm.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class PaymentServiceApplication {

    private PaymentServiceApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }

}
