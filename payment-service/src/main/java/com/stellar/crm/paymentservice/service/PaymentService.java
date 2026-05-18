package com.stellar.crm.paymentservice.service;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.paymentservice.client.xpayment.XPaymentClient;
import com.stellar.crm.paymentservice.client.xpayment.dto.ChargeResponse;
import com.stellar.crm.paymentservice.client.xpayment.dto.CreateChargeRequest;
import com.stellar.crm.paymentservice.dto.PaymentRequest;
import com.stellar.crm.paymentservice.dto.PaymentResponse;
import com.stellar.crm.paymentservice.exception.ResourceNotFoundException;
import com.stellar.crm.paymentservice.kafka.PaymentStatusUpdatedProducer;
import com.stellar.crm.paymentservice.kafka.dto.PaymentStatusUpdated;
import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PendingPaymentCheck;
import com.stellar.crm.paymentservice.repository.PaymentFilter;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import com.stellar.crm.paymentservice.repository.PaymentSpecification;
import com.stellar.crm.paymentservice.repository.PendingPaymentCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String STUB_CUSTOMER = "Unknown Customer";
    private static final String STUB_EMAIL = "noreply@stellar.crm";

    private final PaymentRepository paymentRepository;
    private final PaymentStatusUpdatedProducer paymentStatusUpdatedProducer;
    private final PendingPaymentCheckRepository pendingPaymentCheckRepository;
    private final XPaymentClient xPaymentClient;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        final Payment payment = newPayment(request);
        try {
            final ChargeResponse charge = xPaymentClient.createCharge(buildChargeRequest(payment, request));
            payment.setTransactionRefId(charge.id());
            payment.setStatus(PaymentStatus.PENDING);
            final Payment saved = paymentRepository.save(payment);
            pendingPaymentCheckRepository.save(newQueueEntry(saved));
            return toResponse(saved);
        } catch (RestClientException ex) {
            log.warn("xpayment-api create-charge failed for payment {} (inquiry {})",
                    payment.getId(), request.inquiryRefId(), ex);
            payment.setStatus(PaymentStatus.NOT_SENT);
            final Payment saved = paymentRepository.save(payment);
            paymentStatusUpdatedProducer.send(new PaymentStatusUpdated(
                    saved.getId(),
                    saved.getInquiryRefId(),
                    saved.getStatus(),
                    saved.getUpdatedAt()
            ));
            return toResponse(saved);
        }
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

    private Payment newPayment(PaymentRequest request) {
        final Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInquiryRefId(request.inquiryRefId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return payment;
    }

    private PendingPaymentCheck newQueueEntry(Payment payment) {
        final PendingPaymentCheck entry = new PendingPaymentCheck();
        entry.setPaymentId(payment.getId());
        entry.setInquiryRefId(payment.getInquiryRefId());
        entry.setCreatedAt(Instant.now());
        return entry;
    }

    private CreateChargeRequest buildChargeRequest(Payment payment, PaymentRequest request) {
        return new CreateChargeRequest(
                request.amount(),
                request.currency(),
                STUB_CUSTOMER,
                request.inquiryRefId(),
                STUB_EMAIL,
                Map.of("paymentId", payment.getId().toString())
        );
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
