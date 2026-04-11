package com.stellar.crm.customerservice.service;

import com.stellar.crm.customerservice.model.ContactDetails;
import com.stellar.crm.customerservice.model.Customer;
import com.stellar.crm.customerservice.repository.CustomerRepository;
import com.stellar.crm.customerservice.repository.CustomerSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerServiceTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("customer_db")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private CustomerRepository customerRepository;

    private Customer savedCustomer;

    @DynamicPropertySource
    static void configureProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        savedCustomer = customerRepository.save(buildCustomer("John Doe", "john@example.com", "+1234567890"));
    }

    @Test
    void shouldSaveAndFindById() {
        final Optional<Customer> found = customerRepository.findById(savedCustomer.getGuid());

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().getContactDetails().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldFindByFullNameContains() {
        customerRepository.save(buildCustomer("Jane Smith", "jane@example.com", null));

        final Page<Customer> result = customerRepository.findAll(
                CustomerSpecification.fullNameContains("john"),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldReturnAllWhenSearchTermIsNull() {
        customerRepository.save(buildCustomer("Jane Smith", "jane@example.com", null));

        final Page<Customer> result = customerRepository.findAll(
                CustomerSpecification.fullNameContains(null),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnAllWhenSearchTermIsBlank() {
        customerRepository.save(buildCustomer("Jane Smith", "jane@example.com", null));

        final Page<Customer> result = customerRepository.findAll(
                CustomerSpecification.fullNameContains("   "),
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldReturnPagedResults() {
        customerRepository.save(buildCustomer("Jane Smith", "jane@example.com", null));
        customerRepository.save(buildCustomer("Bob Brown", "bob@example.com", "+9876543210"));
        final int totalCustomers = Math.toIntExact(customerRepository.count());

        final Page<Customer> result = customerRepository.findAll(
                CustomerSpecification.fullNameContains(null),
                PageRequest.of(0, 2, Sort.by("createdAt").descending())
        );

        assertThat(result.getTotalElements()).isEqualTo(totalCustomers);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() {
        final Page<Customer> result = customerRepository.findAll(
                CustomerSpecification.fullNameContains("nonexistent"),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).isEmpty();
    }

    private Customer buildCustomer(final String fullName, final String email, final String phone) {
        final ContactDetails contactDetails = new ContactDetails();
        contactDetails.setGuid(UUID.randomUUID());
        contactDetails.setEmail(email);
        contactDetails.setPhone(phone);
        contactDetails.setCreatedAt(Instant.now());
        contactDetails.setUpdatedAt(Instant.now());

        final Customer customer = new Customer();
        customer.setGuid(UUID.randomUUID());
        customer.setFullName(fullName);
        customer.setContactDetails(contactDetails);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());
        return customer;
    }
}
