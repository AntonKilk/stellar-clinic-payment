package com.stellar.crm.customerservice.service;

import com.stellar.crm.customerservice.dto.ContactDetailsResponse;
import com.stellar.crm.customerservice.dto.CustomerCreateRequest;
import com.stellar.crm.customerservice.dto.CustomerResponse;
import com.stellar.crm.customerservice.dto.CustomerUpdateRequest;
import com.stellar.crm.customerservice.exception.ResourceNotFoundException;
import com.stellar.crm.customerservice.model.ContactDetails;
import com.stellar.crm.customerservice.model.Customer;
import com.stellar.crm.customerservice.repository.CustomerRepository;
import com.stellar.crm.customerservice.repository.CustomerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        ContactDetails contactDetails = new ContactDetails();
        contactDetails.setGuid(UUID.randomUUID());
        contactDetails.setEmail(request.email());
        request.phoneNumber().ifPresent(contactDetails::setPhone);
        contactDetails.setCreatedAt(Instant.now());
        contactDetails.setUpdatedAt(Instant.now());

        Customer customer = new Customer();
        customer.setGuid(UUID.randomUUID());
        customer.setFullName(request.name());
        customer.setContactDetails(contactDetails);
        customer.setCreatedAt(Instant.now());
        customer.setUpdatedAt(Instant.now());

        Customer saved = customerRepository.save(customer);
        return toResponse(saved);
    }

    public Page<CustomerResponse> findAllCustomers(String searchTerm, Pageable pageable) {
        return customerRepository
                .findAll(CustomerSpecification.fullNameContains(searchTerm), pageable)
                .map(this::toResponse);
    }

    public CustomerResponse findCustomerById(UUID guid) {
        final Customer customer = customerRepository.findById(guid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + guid, guid));
        return toResponse(customer);
    }

    public CustomerResponse updateCustomer(UUID guid, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(guid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + guid, guid));

        request.name().ifPresent(customer::setFullName);
        customer.setUpdatedAt(Instant.now());

        ContactDetails contactDetails = customer.getContactDetails();
        request.phoneNumber().ifPresent(contactDetails::setPhone);
        request.email().ifPresent(contactDetails::setEmail);
        contactDetails.setUpdatedAt(Instant.now());

        Customer updated = customerRepository.save(customer);

        return toResponse(updated);
    }

    private CustomerResponse toResponse(Customer customer) {
        ContactDetailsResponse contactDetails = new ContactDetailsResponse(
                customer.getContactDetails().getGuid(),
                customer.getContactDetails().getEmail(),
                customer.getContactDetails().getPhone(),
                customer.getContactDetails().getCreatedAt(),
                customer.getContactDetails().getUpdatedAt()
        );

        return new CustomerResponse(
                customer.getGuid(),
                customer.getFullName(),
                contactDetails,
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
