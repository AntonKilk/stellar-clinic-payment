package com.stellar.crm.inquiryservice.kafka;

import com.stellar.crm.inquiryservice.kafka.dto.CancellationResponse;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquirySource;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancellationResponseListenerTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private CancellationResponseListener listener;

    @Test
    void shouldFlipStatusToCancelledOnSuccess() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.IN_PROGRESS);
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onResponse(new CancellationResponse(
                UUID.randomUUID(), inquiry.getGuid(), UUID.randomUUID(),
                CancellationResponse.Status.SUCCESS, null
        ));

        final ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InquiryStatus.CANCELLED);
        assertThat(captor.getValue().getNote()).isNull();
    }

    @Test
    void shouldFlipStatusToFailedOnNotFoundAndCopyMessageToNote() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.IN_PROGRESS);
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onResponse(new CancellationResponse(
                UUID.randomUUID(), inquiry.getGuid(), UUID.randomUUID(),
                CancellationResponse.Status.NOT_FOUND, "Group not found: abc"
        ));

        final ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InquiryStatus.CANCELLATION_FAILED);
        assertThat(captor.getValue().getNote()).isEqualTo("Group not found: abc");
    }

    @Test
    void shouldFlipStatusToFailedOnAlreadyReleased() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.IN_PROGRESS);
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        listener.onResponse(new CancellationResponse(
                UUID.randomUUID(), inquiry.getGuid(), UUID.randomUUID(),
                CancellationResponse.Status.ALREADY_RELEASED, "Count already zero"
        ));

        final ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InquiryStatus.CANCELLATION_FAILED);
    }

    @Test
    void shouldDropResponseForUnknownInquiry() {
        final UUID unknown = UUID.randomUUID();
        when(inquiryRepository.findById(unknown)).thenReturn(Optional.empty());

        listener.onResponse(new CancellationResponse(
                UUID.randomUUID(), unknown, UUID.randomUUID(),
                CancellationResponse.Status.SUCCESS, null
        ));

        verify(inquiryRepository, never()).save(any());
    }

    private Inquiry buildInquiry(InquiryStatus status) {
        Inquiry inquiry = new Inquiry();
        inquiry.setGuid(UUID.randomUUID());
        inquiry.setProductRefId(UUID.randomUUID());
        inquiry.setCustomerRefId(UUID.randomUUID());
        inquiry.setManagerRefId(UUID.randomUUID());
        inquiry.setSource(InquirySource.CRM);
        inquiry.setStatus(status);
        inquiry.setCreatedAt(Instant.now());
        inquiry.setUpdatedAt(Instant.now());
        return inquiry;
    }
}
