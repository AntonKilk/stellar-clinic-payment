package com.stellar.crm.inquiryservice.service;

import com.stellar.crm.inquiryservice.dto.InquiryCreateRequest;
import com.stellar.crm.inquiryservice.dto.InquiryResponse;
import com.stellar.crm.inquiryservice.dto.InquiryUpdateRequest;
import com.stellar.crm.inquiryservice.exception.ResourceNotFoundException;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryFilter;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import com.stellar.crm.inquiryservice.repository.InquirySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    public InquiryResponse createInquiry(InquiryCreateRequest request) {
        Inquiry inquiry = new Inquiry();
        inquiry.setGuid(UUID.randomUUID());
        inquiry.setProductRefId(request.productRefId());
        inquiry.setCustomerRefId(request.customerRefId());
        inquiry.setGroupRefId(request.groupRefId());
        inquiry.setSource(request.source());
        inquiry.setComment(request.comment());
        inquiry.setStatus(InquiryStatus.NEW);
        inquiry.setManagerRefId(UUID.randomUUID()); // 'TODO': 11/04/2026 when the logic ready add manager ref id here
        inquiry.setCreatedAt(Instant.now());
        inquiry.setUpdatedAt(Instant.now());
        Inquiry saved = inquiryRepository.save(inquiry);
        return toResponse(saved);
    }

    public InquiryResponse updateInquiry(UUID guid, InquiryUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(guid)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with id " + guid, guid));
        if (request.status() != null) {
            inquiry.setStatus(request.status());
        }
        if (request.managerRefId() != null) {
            inquiry.setManagerRefId(request.managerRefId());
        }
        inquiry.setUpdatedAt(Instant.now());
        Inquiry saved = inquiryRepository.save(inquiry);
        return toResponse(saved);
    }

    public InquiryResponse findById(UUID id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with id " + id, id));
        return toResponse(inquiry);
    }

    public Page<InquiryResponse> findAll(InquiryFilter filter, Pageable pageable) {
        return inquiryRepository.findAll(
                        InquirySpecification.byFilter(filter),
                        pageable
                )
                .map(this::toResponse);
    }

    private InquiryResponse toResponse(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getGuid(),
                inquiry.getProductRefId(),
                inquiry.getCustomerRefId(),
                inquiry.getGroupRefId(),
                inquiry.getManagerRefId(),
                inquiry.getSource(),
                inquiry.getComment(),
                inquiry.getStatus(),
                inquiry.getNote(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }
}
