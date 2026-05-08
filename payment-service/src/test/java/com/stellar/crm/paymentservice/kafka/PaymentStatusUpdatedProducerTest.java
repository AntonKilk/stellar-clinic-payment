package com.stellar.crm.paymentservice.kafka;

import com.stellar.crm.paymentservice.kafka.dto.PaymentStatusUpdated;
import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PaymentStatus;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import com.stellar.crm.paymentservice.service.PaymentService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(topics = "payment.status.updated", partitions = 1)
@Testcontainers
class PaymentStatusUpdatedProducerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers"));
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Value("${stellar.kafka.topic.payment-status-updated}")
    private String topic;

    private Consumer<String, PaymentStatusUpdated> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
        paymentRepository.deleteAll();
    }

    @Test
    void shouldPublishEventWhenStatusChanges() {
        final Payment payment = saveReceivedPayment();
        consumer = newConsumer();

        paymentService.updateStatus(payment.getId(), PaymentStatus.APPROVED);

        final ConsumerRecord<String, PaymentStatusUpdated> record =
                KafkaTestUtils.getSingleRecord(consumer, topic, Duration.ofSeconds(10));

        assertThat(record.key()).isEqualTo(payment.getId().toString());
        final PaymentStatusUpdated event = record.value();
        assertThat(event.paymentId()).isEqualTo(payment.getId());
        assertThat(event.inquiryRefId()).isEqualTo(payment.getInquiryRefId());
        assertThat(event.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(event.updatedAt()).isNotNull();
    }

    private Payment saveReceivedPayment() {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.RECEIVED);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return paymentRepository.save(payment);
    }

    private Consumer<String, PaymentStatusUpdated> newConsumer() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps(
                embeddedKafka.getBrokersAsString(), "test-" + UUID.randomUUID(), "true"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put("spring.json.value.default.type", PaymentStatusUpdated.class.getName());
        props.put("spring.json.trusted.packages", "com.stellar.crm.paymentservice.kafka.dto");
        props.put("spring.json.use.type.headers", false);

        Consumer<String, PaymentStatusUpdated> c =
                new DefaultKafkaConsumerFactory<String, PaymentStatusUpdated>(props).createConsumer();
        c.subscribe(List.of(topic));
        return c;
    }
}