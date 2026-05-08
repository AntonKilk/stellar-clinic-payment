package com.stellar.crm.paymentservice.service;

import com.stellar.crm.paymentservice.dto.PaymentRequest;
import com.stellar.crm.paymentservice.dto.PaymentResponse;
import com.stellar.crm.paymentservice.exception.ResourceNotFoundException;
import com.stellar.crm.paymentservice.kafka.PaymentStatusUpdatedProducer;
import com.stellar.crm.paymentservice.kafka.dto.PaymentStatusUpdated;
import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PaymentStatus;
import com.stellar.crm.paymentservice.repository.PaymentFilter;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import com.stellar.crm.paymentservice.repository.PaymentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentStatusUpdatedProducer paymentStatusUpdatedProducer;

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
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id, id));
        return toResponse(payment);
    }

    public Page<PaymentResponse> findAll(final PaymentFilter filter, final Pageable pageable) {
        return paymentRepository.findAll(
                        PaymentSpecification.byFilter(filter),
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional
    public PaymentResponse updateStatus(UUID id, PaymentStatus newStatus) {
        final Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id, id));

        if (payment.getStatus() == newStatus) {
            return toResponse(payment);
        }

        payment.setStatus(newStatus);
        payment.setUpdatedAt(Instant.now());
        final Payment updated = paymentRepository.save(payment);

        paymentStatusUpdatedProducer.send(new PaymentStatusUpdated(
                updated.getId(),
                updated.getInquiryRefId(),
                updated.getStatus(),
                updated.getUpdatedAt()
        ));

        return toResponse(updated);
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
