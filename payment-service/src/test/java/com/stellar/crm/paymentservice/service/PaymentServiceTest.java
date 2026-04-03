package com.stellar.crm.paymentservice.service;

import com.stellar.crm.paymentservice.dto.PaymentResponse;
import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PaymentStatus;
import com.stellar.crm.paymentservice.repository.PaymentFilter;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private static final int PAGE_SIZE_DEFAULT = 10;
    private static final int PAGE_SIZE_MEDIUM = 25;
    private static final int TOTAL_ELEMENTS = 100;
    private static final int SECONDS_AGO = 60;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldReturnPagedAndFilteredResult() {
        PaymentFilter filter = new PaymentFilter(null, null, PaymentStatus.RECEIVED, null, null);
        PageRequest pageable = PageRequest.of(0, PAGE_SIZE_DEFAULT, Sort.by("createdAt").descending());
        List<Payment> payments = List.of(
                buildPayment(PaymentStatus.RECEIVED),
                buildPayment(PaymentStatus.RECEIVED)
        );
        Page<Payment> paymentPage = new PageImpl(payments, pageable, payments.size());

        when(paymentRepository.findAll(
                ArgumentMatchers.<Specification<Payment>>any(),
                ArgumentMatchers.eq(pageable)
        )).thenReturn(paymentPage);

        Page<PaymentResponse> result = paymentService.findAll(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).status()).isEqualTo((PaymentStatus.RECEIVED));
    }

    @Test
    void shouldReturnEmptyPageWhenNoMatch() {
        PaymentFilter filter = new PaymentFilter(null, null, PaymentStatus.DECLINED, null, null);
        PageRequest pageable = PageRequest.of(0, PAGE_SIZE_DEFAULT);
        Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(paymentRepository.findAll(
                ArgumentMatchers.<Specification<Payment>>any(),
                ArgumentMatchers.eq(pageable)
        )).thenReturn(emptyPage);

        Page<PaymentResponse> result = paymentService.findAll(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void shouldRespectPageSize() {
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null);
        PageRequest pageable = PageRequest.of(0, PAGE_SIZE_MEDIUM);
        List<Payment> payments = List.of(
                buildPayment(PaymentStatus.RECEIVED),
                buildPayment(PaymentStatus.PENDING)
        );
        Page<Payment> paymentsPage = new PageImpl<>(payments, pageable, TOTAL_ELEMENTS);

        when(paymentRepository.findAll(
                ArgumentMatchers.<Specification<Payment>>any(),
                ArgumentMatchers.eq(pageable)
        )).thenReturn(paymentsPage);

        final Page<PaymentResponse> result = paymentService.findAll(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(TOTAL_ELEMENTS);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPageable().getPageSize()).isEqualTo(PAGE_SIZE_MEDIUM);
    }

    @Test
    void shouldSortByCreatedAtDescending() {
        Instant older = Instant.now().minusSeconds(SECONDS_AGO);
        Instant newer = Instant.now();
        PaymentFilter filter = new PaymentFilter(null, null, null, null, null);
        PageRequest pageable = PageRequest.of(0, PAGE_SIZE_DEFAULT, Sort.by("createdAt").descending());

        Payment olderPayment = buildPayment(PaymentStatus.RECEIVED);
        olderPayment.setCreatedAt(older);
        Payment newerPayment = buildPayment(PaymentStatus.PENDING);
        newerPayment.setCreatedAt(newer);

        Page<Payment> paymentsPage = new PageImpl<>(
                List.of(newerPayment, olderPayment), pageable, 2
        );

        when(paymentRepository.findAll(
                ArgumentMatchers.<Specification<Payment>>any(),
                ArgumentMatchers.eq(pageable)
        )).thenReturn(paymentsPage);

        Page<PaymentResponse> result = paymentService.findAll(filter, pageable);

        assertThat(result.getContent().get(0).createdAt()).isAfter(result.getContent().get(1).createdAt());
    }

    private Payment buildPayment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(status);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return payment;
    }
}
