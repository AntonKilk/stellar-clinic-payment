package com.stellar.crm.inventoryservice.kafka;

import com.stellar.crm.inventoryservice.kafka.dto.CancellationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancellationResponseProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${stellar.kafka.topic.cancellation-response}")
    private String topic;

    public void send(CancellationResponse response) {
        kafkaTemplate.send(topic, response.inquiryId().toString(), response)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Async send failure for cancellation response {} (inquiry {})",
                                response.correlationId(), response.inquiryId(), ex);
                    }
                });
    }
}
