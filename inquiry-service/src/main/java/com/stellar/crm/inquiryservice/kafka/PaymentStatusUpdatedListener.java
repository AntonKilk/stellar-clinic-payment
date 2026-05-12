package com.stellar.crm.inquiryservice.kafka;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.inquiryservice.kafka.dto.PaymentStatusUpdated;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusUpdatedListener {

    private final InquiryRepository inquiryRepository;

    @KafkaListener(
            topics = "${stellar.kafka.topic.payment-status-updated}",
            containerFactory = "paymentStatusUpdatedListenerContainerFactory"
    )
    @Transactional
    public void onEvent(PaymentStatusUpdated event) {
        Optional<Inquiry> maybe = inquiryRepository.findById(event.inquiryRefId());
        if (maybe.isEmpty()) {
            return;
        }

        InquiryStatus mapped = mapStatus(event.status());
        Inquiry inquiry = maybe.get();
        if (inquiry.getStatus() == mapped) {
            return;
        }

        inquiry.setStatus(mapped);
        if (event.status() == PaymentStatus.DECLINED || event.status() == PaymentStatus.NOT_SENT) {
            inquiry.setNote("Payment " + event.status());
        }
        inquiry.setUpdatedAt(Instant.now());
        inquiryRepository.save(inquiry);
    }

    private InquiryStatus mapStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case RECEIVED, PENDING -> InquiryStatus.PAYMENT;
            case APPROVED -> InquiryStatus.PAID;
            case DECLINED, NOT_SENT -> InquiryStatus.REJECTED;
        };
    }
}
