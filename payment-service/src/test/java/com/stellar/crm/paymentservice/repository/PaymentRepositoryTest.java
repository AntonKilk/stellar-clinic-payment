package com.stellar.crm.paymentservice.repository;

import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("payment_db")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment savedPayment;

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        savedPayment = paymentRepository.save(buildPayment(PaymentStatus.RECEIVED));
    }

    @Test
    void shouldSaveAndFindById() {
        final Optional<Payment> found = paymentRepository.findById(savedPayment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getInquiryRefId()).isEqualTo(savedPayment.getInquiryRefId());
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.RECEIVED);
    }

    @Test
    void shouldFindByStatus() {
        paymentRepository.save(buildPayment(PaymentStatus.PENDING));

        final PaymentFilter filter = new PaymentFilter(null, null, PaymentStatus.RECEIVED, null, null);
        final Page<Payment> result = paymentRepository.findAll(
                PaymentSpecification.byFilter(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(PaymentStatus.RECEIVED);
    }

    @Test
    void shouldFindByInquiryRefId() {
        final UUID targetInquiryId = savedPayment.getInquiryRefId();
        paymentRepository.save(buildPayment(PaymentStatus.PENDING));

        final PaymentFilter filter = new PaymentFilter(null, targetInquiryId, null, null, null);
        final Page<Payment> result = paymentRepository.findAll(
                PaymentSpecification.byFilter(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getInquiryRefId()).isEqualTo(targetInquiryId);
    }

    @Test
    void shouldFindByCreatedAtRange() {
        final Instant from = Instant.now().minusSeconds(60);
        final Instant to = Instant.now().plusSeconds(60);

        final PaymentFilter filter = new PaymentFilter(null, null, null, from, to);
        final Page<Payment> result = paymentRepository.findAll(
                PaymentSpecification.byFilter(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void shouldReturnPagedResults() {
        paymentRepository.save(buildPayment(PaymentStatus.PENDING));
        paymentRepository.save(buildPayment(PaymentStatus.APPROVED));
        int totalTests = Math.toIntExact(paymentRepository.count());
        final PaymentFilter filter = new PaymentFilter(null, null, null, null, null);
        final Page<Payment> result = paymentRepository.findAll(
                PaymentSpecification.byFilter(filter),
                PageRequest.of(0, 2, Sort.by("createdAt").descending())
        );

        assertThat(result.getTotalElements()).isEqualTo(totalTests);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        final PaymentFilter filter = new PaymentFilter(null, null, PaymentStatus.DECLINED, null, null);
        final Page<Payment> result = paymentRepository.findAll(
                PaymentSpecification.byFilter(filter),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    private Payment buildPayment(final PaymentStatus status) {
        final Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(status);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return payment;
    }
}
