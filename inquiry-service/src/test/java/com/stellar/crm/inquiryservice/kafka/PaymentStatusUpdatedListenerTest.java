package com.stellar.crm.inquiryservice.kafka;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.inquiryservice.kafka.dto.PaymentStatusUpdated;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquirySource;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(topics = "payment.status.updated", partitions = 1)
@Testcontainers
class PaymentStatusUpdatedListenerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Value("${stellar.kafka.topic.payment-status-updated}")
    private String topic;

    private Producer<String, Object> producer;

    @AfterEach
    void tearDown() {
        if (producer != null) {
            producer.close();
        }
        inquiryRepository.deleteAll();
    }

    @Test
    void shouldFlipInquiryToPaidOnApprovedEvent() {
        final Inquiry inquiry = saveInquiry(InquiryStatus.PAYMENT);
        producer = newProducer();

        publish(new PaymentStatusUpdated(
                UUID.randomUUID(),
                inquiry.getGuid(),
                PaymentStatus.APPROVED,
                Instant.now()
        ));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    final Inquiry updated = inquiryRepository.findById(inquiry.getGuid()).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(InquiryStatus.PAID);
                    assertThat(updated.getNote()).isNull();
                });
    }

    @Test
    void shouldFlipInquiryToRejectedAndSetNoteOnDeclinedEvent() {
        final Inquiry inquiry = saveInquiry(InquiryStatus.PAYMENT);
        producer = newProducer();

        publish(new PaymentStatusUpdated(
                UUID.randomUUID(),
                inquiry.getGuid(),
                PaymentStatus.DECLINED,
                Instant.now()
        ));

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    final Inquiry updated = inquiryRepository.findById(inquiry.getGuid()).orElseThrow();
                    assertThat(updated.getStatus()).isEqualTo(InquiryStatus.REJECTED);
                    assertThat(updated.getNote()).contains("DECLINED");
                });
    }

    private void publish(PaymentStatusUpdated event) {
        producer.send(new ProducerRecord<>(topic, event.paymentId().toString(), event));
        producer.flush();
    }

    private Inquiry saveInquiry(InquiryStatus status) {
        Inquiry inquiry = new Inquiry();
        inquiry.setGuid(UUID.randomUUID());
        inquiry.setProductRefId(UUID.randomUUID());
        inquiry.setCustomerRefId(UUID.randomUUID());
        inquiry.setManagerRefId(UUID.randomUUID());
        inquiry.setSource(InquirySource.CRM);
        inquiry.setStatus(status);
        inquiry.setCreatedAt(Instant.now());
        inquiry.setUpdatedAt(Instant.now());
        return inquiryRepository.save(inquiry);
    }

    private Producer<String, Object> newProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<String, Object>(props).createProducer();
    }
}
