package com.stellar.crm.inquiryservice.kafka;

import com.stellar.crm.inquiryservice.exception.ExternalServiceException;
import com.stellar.crm.inquiryservice.kafka.dto.CancellationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancellationRequestProducer {

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${stellar.kafka.topic.cancellation-request}")
    private String topic;

    public void send(CancellationRequest request) {
        try {
            kafkaTemplate.send(topic, request.inquiryId().toString(), request)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Async send failure for cancellation request {} (inquiry {})",
                                    request.correlationId(), request.inquiryId(), ex);
                        }
                    });
        } catch (KafkaException ex) {
            throw new ExternalServiceException(
                    "Failed to publish cancellation request: " + ex.getMessage());
        }
    }
}
