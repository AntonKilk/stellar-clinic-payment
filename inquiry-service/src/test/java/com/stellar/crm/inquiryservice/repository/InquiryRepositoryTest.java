package com.stellar.crm.inquiryservice.repository;

import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquirySource;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InquiryRepositoryTest {

    private static final int PAGE_SIZE = 10;

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("inquiry_db")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private InquiryRepository inquiryRepository;

    private UUID customerRefId;
    private UUID managerRefId;
    private UUID productRefId;
    private Inquiry savedInquiry;

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        inquiryRepository.deleteAll();
        customerRefId = UUID.randomUUID();
        managerRefId = UUID.randomUUID();
        productRefId = UUID.randomUUID();
        savedInquiry = inquiryRepository.save(
                buildInquiry(customerRefId, managerRefId, productRefId, InquiryStatus.NEW)
        );
    }

    @Test
    void shouldReturnInquiriesMatchingCustomerRefId() {
        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, customerRefId, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getGuid()).isEqualTo(savedInquiry.getGuid());
    }

    @Test
    void shouldNotReturnInquiriesWithDifferentCustomerRefId() {
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), managerRefId, UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, customerRefId, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCustomerRefId()).isEqualTo(customerRefId);
    }

    @Test
    void shouldReturnAllWhenCustomerRefIdIsNull() {
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), managerRefId, UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnInquiriesMatchingManagerRefId() {
        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, managerRefId, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getManagerRefId()).isEqualTo(managerRefId);
    }

    @Test
    void shouldNotReturnInquiriesWithDifferentManagerRefId() {
        inquiryRepository.save(buildInquiry(customerRefId, UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, managerRefId, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getManagerRefId()).isEqualTo(managerRefId);
    }

    @Test
    void shouldReturnAllWhenManagerRefIdIsNull() {
        inquiryRepository.save(buildInquiry(customerRefId, UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnInquiriesMatchingStatus() {
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.REJECTED));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(InquiryStatus.NEW, null, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(InquiryStatus.NEW);
    }

    @Test
    void shouldReturnAllWhenStatusIsNull() {
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.IN_PROGRESS));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldFilterByCustomerRefIdAndStatus() {
        inquiryRepository.save(buildInquiry(customerRefId, UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.REJECTED));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(InquiryStatus.NEW, customerRefId, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getGuid()).isEqualTo(savedInquiry.getGuid());
    }

    @Test
    void shouldFilterByAllThreeFields() {
        inquiryRepository.save(buildInquiry(customerRefId, UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.NEW));
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), managerRefId, UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(InquiryStatus.NEW, customerRefId, managerRefId, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getGuid()).isEqualTo(savedInquiry.getGuid());
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(InquiryStatus.PAID, customerRefId, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldReturnInquiriesMatchingProductRefId() {
        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, null, productRefId)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductRefId()).isEqualTo(productRefId);
    }

    @Test
    void shouldNotReturnInquiriesWithDifferentProductRefId() {
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, null, productRefId)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductRefId()).isEqualTo(productRefId);
    }

    @Test
    void shouldReturnAllWhenProductRefIdIsNull() {
        inquiryRepository.save(buildInquiry(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), InquiryStatus.NEW));

        final Page<Inquiry> result = inquiryRepository.findAll(
                InquirySpecification.byFilter(new InquiryFilter(null, null, null, null)),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    private Inquiry buildInquiry(
            final UUID customer,
            final UUID manager,
            final UUID product,
            final InquiryStatus status
    ) {
        final Inquiry inquiry = new Inquiry();
        inquiry.setGuid(UUID.randomUUID());
        inquiry.setProductRefId(product);
        inquiry.setCustomerRefId(customer);
        inquiry.setManagerRefId(manager);
        inquiry.setSource(InquirySource.CRM);
        inquiry.setStatus(status);
        inquiry.setCreatedAt(Instant.now());
        inquiry.setUpdatedAt(Instant.now());
        return inquiry;
    }
}
