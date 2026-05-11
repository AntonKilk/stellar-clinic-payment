package com.stellar.crm.inquiryservice.kafka;

import com.stellar.crm.inquiryservice.kafka.dto.CancellationResponse;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancellationResponseListener {

    private final InquiryRepository inquiryRepository;

    @KafkaListener(topics = "${stellar.kafka.topic.cancellation-response}")
    @Transactional
    public void onResponse(CancellationResponse response) {
        Optional<Inquiry> maybe = inquiryRepository.findById(response.inquiryId());
        if (maybe.isEmpty()) {
            return;
        }
        Inquiry inquiry = maybe.get();
        inquiry.setStatus(response.status() == CancellationResponse.Status.SUCCESS
                ? InquiryStatus.CANCELLED
                : InquiryStatus.CANCELLATION_FAILED);
        if (response.message() != null) {
            inquiry.setNote(response.message());
        }
        inquiry.setUpdatedAt(Instant.now());
        inquiryRepository.save(inquiry);
    }
}
