package com.stellar.crm.inquiryservice.kafka.config;

import com.stellar.crm.inquiryservice.kafka.dto.PaymentStatusUpdated;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentStatusUpdatedKafkaConfig {

    @Bean
    public ConsumerFactory<String, PaymentStatusUpdated> paymentStatusUpdatedConsumerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());
        config.put("spring.json.value.default.type", PaymentStatusUpdated.class.getName());
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentStatusUpdated>
    paymentStatusUpdatedListenerContainerFactory(
            ConsumerFactory<String, PaymentStatusUpdated> paymentStatusUpdatedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentStatusUpdated> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentStatusUpdatedConsumerFactory);
        return factory;
    }
}
