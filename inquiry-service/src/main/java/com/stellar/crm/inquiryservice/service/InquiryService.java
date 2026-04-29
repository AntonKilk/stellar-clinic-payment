package com.stellar.crm.inquiryservice.service;

import com.stellar.crm.inquiryservice.dto.InquiryCreateRequest;
import com.stellar.crm.inquiryservice.dto.InquiryResponse;
import com.stellar.crm.inquiryservice.dto.InquiryUpdateRequest;
import com.stellar.crm.inquiryservice.exception.ResourceNotFoundException;
import com.stellar.crm.inquiryservice.kafka.CancellationRequestProducer;
import com.stellar.crm.inquiryservice.kafka.dto.CancellationRequest;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryFilter;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import com.stellar.crm.inquiryservice.repository.InquirySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private static final Set<InquiryStatus> NON_CANCELLABLE = Set.of(
            InquiryStatus.PAID,
            InquiryStatus.REJECTED,
            InquiryStatus.CANCELLED,
            InquiryStatus.CANCELLATION_FAILED
    );

    private final InquiryRepository inquiryRepository;
    private final CancellationRequestProducer cancellationRequestProducer;

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

    @Transactional
    public void requestCancellation(UUID id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found with id " + id, id));

        if (NON_CANCELLABLE.contains(inquiry.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inquiry " + id + " cannot be cancelled in status " + inquiry.getStatus());
        }

        if (inquiry.getGroupRefId() == null) {
            inquiry.setStatus(InquiryStatus.CANCELLED);
            inquiry.setUpdatedAt(Instant.now());
            inquiryRepository.save(inquiry);
            return;
        }

        cancellationRequestProducer.send(new CancellationRequest(
                UUID.randomUUID(),
                inquiry.getGuid(),
                inquiry.getGroupRefId(),
                Instant.now()
        ));
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
