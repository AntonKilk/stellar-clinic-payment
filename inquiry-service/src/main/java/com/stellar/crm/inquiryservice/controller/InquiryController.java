package com.stellar.crm.inquiryservice.controller;

import com.stellar.crm.inquiryservice.dto.InquiryCreateRequest;
import com.stellar.crm.inquiryservice.dto.InquiryResponse;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryFilter;
import com.stellar.crm.inquiryservice.service.InquiryService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "status");
    private static final int PAGE_SIZE = 10;
    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<Page<InquiryResponse>> getAll(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) UUID customerRefId,
            @RequestParam(required = false) UUID productRefId,
            @ParameterObject @PageableDefault(size = PAGE_SIZE) Pageable pageable) {
        validateSortFields(pageable);
        final var filter = new InquiryFilter(status, customerRefId, null, productRefId);
        return ResponseEntity.ok(inquiryService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inquiryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<InquiryResponse> create(@Valid @RequestBody InquiryCreateRequest request) {
        return status(HttpStatus.CREATED).body(inquiryService.createInquiry(request));
    }

    private void validateSortFields(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid sort field '" + order.getProperty() + "'. Allowed: " + ALLOWED_SORT_FIELDS);
            }
        });
    }
}
