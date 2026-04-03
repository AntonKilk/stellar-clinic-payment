package com.stellar.crm.paymentservice.service;

import com.stellar.crm.paymentservice.dto.PaymentRequest;
import com.stellar.crm.paymentservice.dto.PaymentResponse;
import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PaymentStatus;
import com.stellar.crm.paymentservice.repository.PaymentFilter;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import com.stellar.crm.paymentservice.repository.PaymentSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentResponse createPayment(PaymentRequest request) {
        final Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInquiryRefId(request.inquiryRefId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setStatus(PaymentStatus.RECEIVED);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        final Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    public PaymentResponse findById(UUID id) {
        final Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + id));
        return toResponse(payment);
    }

    public Page<PaymentResponse> findAll(final PaymentFilter filter, final Pageable pageable) {
        return paymentRepository.findAll(
                        PaymentSpecification.byFilter(filter),
                        pageable
                )
                .map(this::toResponse);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInquiryRefId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionRefId(),
                payment.getStatus(),
                payment.getNote(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
