package com.stellar.crm.inquiryservice.service;

import com.stellar.crm.inquiryservice.dto.InquiryCreateRequest;
import com.stellar.crm.inquiryservice.dto.InquiryResponse;
import com.stellar.crm.inquiryservice.dto.InquiryUpdateRequest;
import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquirySource;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import com.stellar.crm.inquiryservice.repository.InquiryFilter;
import com.stellar.crm.inquiryservice.repository.InquiryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    private static final int PAGE_SIZE = 10;

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private InquiryService inquiryService;

    @Test
    void shouldCreateInquiryWithCorrectFields() {
        final InquiryCreateRequest request = new InquiryCreateRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), InquirySource.CRM, "test comment"
        );
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final InquiryResponse response = inquiryService.createInquiry(request);

        assertThat(response.productRefId()).isEqualTo(request.productRefId());
        assertThat(response.customerRefId()).isEqualTo(request.customerRefId());
        assertThat(response.groupRefId()).isEqualTo(request.groupRefId());
        assertThat(response.source()).isEqualTo(InquirySource.CRM);
        assertThat(response.comment()).isEqualTo("test comment");
        assertThat(response.status()).isEqualTo(InquiryStatus.NEW);
        assertThat(response.guid()).isNotNull();
        assertThat(response.managerRefId()).isNotNull();
    }

    @Test
    void shouldPersistInquiryOnCreate() {
        final InquiryCreateRequest request = new InquiryCreateRequest(
                UUID.randomUUID(), UUID.randomUUID(), null, InquirySource.TELEGRAM, null
        );
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inquiryService.createInquiry(request);

        final ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InquiryStatus.NEW);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
        assertThat(captor.getValue().getGuid()).isNotNull();
    }

    @Test
    void shouldUpdateStatusWhenProvided() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.NEW);
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final InquiryResponse response = inquiryService.updateInquiry(
                inquiry.getGuid(), new InquiryUpdateRequest(null, InquiryStatus.IN_PROGRESS)
        );

        assertThat(response.status()).isEqualTo(InquiryStatus.IN_PROGRESS);
    }

    @Test
    void shouldUpdateManagerRefIdWhenProvided() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.NEW);
        final UUID newManagerRefId = UUID.randomUUID();
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final InquiryResponse response = inquiryService.updateInquiry(
                inquiry.getGuid(), new InquiryUpdateRequest(newManagerRefId, null)
        );

        assertThat(response.managerRefId()).isEqualTo(newManagerRefId);
    }

    @Test
    void shouldNotChangeStatusWhenNullOnUpdate() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.PAYMENT);
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final InquiryResponse response = inquiryService.updateInquiry(
                inquiry.getGuid(), new InquiryUpdateRequest(null, null)
        );

        assertThat(response.status()).isEqualTo(InquiryStatus.PAYMENT);
    }

    @Test
    void shouldThrowWhenInquiryNotFoundOnUpdate() {
        final UUID guid = UUID.randomUUID();
        when(inquiryRepository.findById(guid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.updateInquiry(guid, new InquiryUpdateRequest(null, null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldReturnInquiryWhenFound() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.NEW);
        when(inquiryRepository.findById(inquiry.getGuid())).thenReturn(Optional.of(inquiry));

        final InquiryResponse response = inquiryService.findById(inquiry.getGuid());

        assertThat(response.guid()).isEqualTo(inquiry.getGuid());
        assertThat(response.status()).isEqualTo(InquiryStatus.NEW);
    }

    @Test
    void shouldThrowWhenInquiryNotFoundById() {
        final UUID guid = UUID.randomUUID();
        when(inquiryRepository.findById(guid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inquiryService.findById(guid))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(guid.toString());
    }

    @Test
    void shouldReturnMappedPageOnFindAll() {
        final Inquiry inquiry = buildInquiry(InquiryStatus.NEW);
        when(inquiryRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(inquiry)));

        final Page<InquiryResponse> result = inquiryService.findAll(
                new InquiryFilter(null, null, null, null),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).guid()).isEqualTo(inquiry.getGuid());
    }

    @Test
    void shouldReturnEmptyPageWhenNoInquiriesFound() {
        when(inquiryRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        final Page<InquiryResponse> result = inquiryService.findAll(
                new InquiryFilter(InquiryStatus.PAID, UUID.randomUUID(), UUID.randomUUID(), null),
                PageRequest.of(0, PAGE_SIZE)
        );

        assertThat(result.getContent()).isEmpty();
    }

    private Inquiry buildInquiry(final InquiryStatus status) {
        final Inquiry inquiry = new Inquiry();
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
