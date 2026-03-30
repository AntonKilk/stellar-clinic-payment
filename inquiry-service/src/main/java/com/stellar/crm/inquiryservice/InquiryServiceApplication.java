package com.stellar.crm.inquiryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InquiryServiceApplication {

    private InquiryServiceApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(InquiryServiceApplication.class, args);
    }

}
