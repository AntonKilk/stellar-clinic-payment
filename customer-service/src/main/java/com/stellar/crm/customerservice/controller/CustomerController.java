package com.stellar.crm.customerservice.controller;

import com.stellar.crm.customerservice.dto.CustomerCreateRequest;
import com.stellar.crm.customerservice.dto.CustomerResponse;
import com.stellar.crm.customerservice.dto.CustomerUpdateRequest;
import com.stellar.crm.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    private static final int PAGE_SIZE = 25;
    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String fullName,
            @ParameterObject @PageableDefault(size = PAGE_SIZE) Pageable pageable
    ) {
        return ResponseEntity.ok(customerService.findAllCustomers(fullName, pageable));
    }

    @GetMapping("/{guid}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID guid) {
        return ResponseEntity.ok(customerService.findCustomerById(guid));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        return status(HttpStatus.CREATED).body(customerService.createCustomer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID id, @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return status(HttpStatus.OK).body(customerService.updateCustomer(id, request));
    }
}
