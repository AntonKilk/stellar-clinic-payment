package com.stellar.crm.paymentservice.service;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.paymentservice.client.xpayment.XPaymentClient;
import com.stellar.crm.paymentservice.client.xpayment.dto.ChargeResponse;
import com.stellar.crm.paymentservice.client.xpayment.dto.ChargeStatus;
import com.stellar.crm.paymentservice.model.Payment;
import com.stellar.crm.paymentservice.model.PendingPaymentCheck;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import com.stellar.crm.paymentservice.repository.PendingPaymentCheckRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentStatusPollingJobTest {

    @Mock
    private PendingPaymentCheckRepository pendingPaymentCheckRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private XPaymentClient xPaymentClient;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentStatusPollingJob job;

    @Test
    void shouldDoNothingWhenQueueIsEmpty() {
        when(pendingPaymentCheckRepository.findAll()).thenReturn(List.of());

        job.pollPendingPayments();

        verifyNoInteractions(paymentRepository, xPaymentClient, paymentService);
    }

    @Test
    void shouldUpdateAndDeleteWhenChargeIsSucceeded() {
        final Payment payment = buildPayment();
        final PendingPaymentCheck entry = buildQueueEntry(payment);
        final ChargeResponse charge = buildCharge(payment.getTransactionRefId(), ChargeStatus.SUCCEEDED);

        when(pendingPaymentCheckRepository.findAll()).thenReturn(List.of(entry));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(xPaymentClient.getCharge(payment.getTransactionRefId())).thenReturn(charge);

        job.pollPendingPayments();

        verify(paymentService).updateStatus(payment.getId(), PaymentStatus.APPROVED);
        verify(pendingPaymentCheckRepository).deleteById(payment.getId());
    }

    @Test
    void shouldUpdateAndDeleteWhenChargeIsCanceled() {
        final Payment payment = buildPayment();
        final PendingPaymentCheck entry = buildQueueEntry(payment);
        final ChargeResponse charge = buildCharge(payment.getTransactionRefId(), ChargeStatus.CANCELED);

        when(pendingPaymentCheckRepository.findAll()).thenReturn(List.of(entry));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(xPaymentClient.getCharge(payment.getTransactionRefId())).thenReturn(charge);

        job.pollPendingPayments();

        verify(paymentService).updateStatus(payment.getId(), PaymentStatus.DECLINED);
        verify(pendingPaymentCheckRepository).deleteById(payment.getId());
    }

    @Test
    void shouldKeepEntryWhenChargeIsStillProcessing() {
        final Payment payment = buildPayment();
        final PendingPaymentCheck entry = buildQueueEntry(payment);
        final ChargeResponse charge = buildCharge(payment.getTransactionRefId(), ChargeStatus.PROCESSING);

        when(pendingPaymentCheckRepository.findAll()).thenReturn(List.of(entry));
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(xPaymentClient.getCharge(payment.getTransactionRefId())).thenReturn(charge);

        job.pollPendingPayments();

        verify(paymentService, never()).updateStatus(any(), any());
        verify(pendingPaymentCheckRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteStaleEntryWhenPaymentIsMissing() {
        final UUID paymentId = UUID.randomUUID();
        final PendingPaymentCheck entry = new PendingPaymentCheck();
        entry.setPaymentId(paymentId);
        entry.setInquiryRefId(UUID.randomUUID());
        entry.setCreatedAt(Instant.now());

        when(pendingPaymentCheckRepository.findAll()).thenReturn(List.of(entry));
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        job.pollPendingPayments();

        verify(pendingPaymentCheckRepository).deleteById(paymentId);
        verifyNoInteractions(xPaymentClient);
        verify(paymentService, never()).updateStatus(any(), any());
    }

    @Test
    void shouldContinueBatchWhenProviderFailsOnOneEntry() {
        final Payment failing = buildPayment();
        final Payment succeeding = buildPayment();
        final PendingPaymentCheck failingEntry = buildQueueEntry(failing);
        final PendingPaymentCheck succeedingEntry = buildQueueEntry(succeeding);
        final ChargeResponse succeedingCharge = buildCharge(succeeding.getTransactionRefId(),
                ChargeStatus.SUCCEEDED);

        when(pendingPaymentCheckRepository.findAll()).thenReturn(List.of(failingEntry, succeedingEntry));
        when(paymentRepository.findById(failing.getId())).thenReturn(Optional.of(failing));
        when(paymentRepository.findById(succeeding.getId())).thenReturn(Optional.of(succeeding));
        when(xPaymentClient.getCharge(failing.getTransactionRefId()))
                .thenThrow(new RestClientException("provider down"));
        when(xPaymentClient.getCharge(succeeding.getTransactionRefId())).thenReturn(succeedingCharge);

        job.pollPendingPayments();

        verify(paymentService, times(1)).updateStatus(any(), any());
        verify(paymentService).updateStatus(succeeding.getId(), PaymentStatus.APPROVED);
        verify(pendingPaymentCheckRepository).deleteById(succeeding.getId());
    }

    private Payment buildPayment() {
        final Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setInquiryRefId(UUID.randomUUID());
        payment.setTransactionRefId(UUID.randomUUID());
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        return payment;
    }

    private PendingPaymentCheck buildQueueEntry(Payment payment) {
        final PendingPaymentCheck entry = new PendingPaymentCheck();
        entry.setPaymentId(payment.getId());
        entry.setInquiryRefId(payment.getInquiryRefId());
        entry.setCreatedAt(Instant.now());
        return entry;
    }

    private ChargeResponse buildCharge(UUID id, ChargeStatus status) {
        return new ChargeResponse(
                id,
                new BigDecimal("100.00"),
                "USD",
                new BigDecimal("100.00"),
                Instant.now(),
                Instant.now(),
                "Unknown Customer",
                UUID.randomUUID(),
                "noreply@stellar.crm",
                status,
                Map.of()
        );
    }
}
