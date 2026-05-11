package com.stellar.crm.paymentservice.kafka;

import com.stellar.crm.paymentservice.kafka.dto.PaymentStatusUpdated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusUpdatedProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${stellar.kafka.topic.payment-status-updated}")
    private String topic;

    public void send(PaymentStatusUpdated event) {
        kafkaTemplate.send(topic, event.paymentId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Async send failure for payment status update {} (inquiry {})",
                                event.paymentId(), event.inquiryRefId(), ex);
                    }
                });
    }
}
