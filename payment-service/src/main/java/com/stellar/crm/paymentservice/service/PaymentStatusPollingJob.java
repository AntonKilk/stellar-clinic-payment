package com.stellar.crm.paymentservice.service;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.paymentservice.client.xpayment.ChargeStatusMapper;
import com.stellar.crm.paymentservice.client.xpayment.XPaymentClient;
import com.stellar.crm.paymentservice.client.xpayment.dto.ChargeResponse;
import com.stellar.crm.paymentservice.model.PendingPaymentCheck;
import com.stellar.crm.paymentservice.repository.PaymentRepository;
import com.stellar.crm.paymentservice.repository.PendingPaymentCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStatusPollingJob {

    private final PendingPaymentCheckRepository pendingPaymentCheckRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final XPaymentClient xPaymentClient;

    @Scheduled(fixedDelayString = "${stellar.payment.polling.interval-ms:300000}")
    public void pollPendingPayments() {
        final List<PendingPaymentCheck> pending = pendingPaymentCheckRepository.findAll();
        if (pending.isEmpty()) {
            return;
        }
        log.info("Polling {} pending payment(s)", pending.size());

        for (PendingPaymentCheck entry : pending) {
            try {
                processEntry(entry);
            } catch (Exception e) {
                log.warn("Failed to poll payment {} (inquiry {})",
                        entry.getPaymentId(), entry.getInquiryRefId());
            }
        }
    }

    private void processEntry(PendingPaymentCheck entry) {
        final UUID paymentId = entry.getPaymentId();
        final Optional<UUID> transactionRefId = paymentRepository.findById(paymentId)
                .map(p -> p.getTransactionRefId());
        if (transactionRefId.isEmpty() || transactionRefId.get() == null) {
            log.warn("Payment {} has no transaction ref ID or is missing, removing stale queue entry", paymentId);
            pendingPaymentCheckRepository.deleteById(paymentId);
            return;
        }
        final ChargeResponse charge = xPaymentClient.getCharge(transactionRefId.get());

        if (!ChargeStatusMapper.isTerminal(charge.status())) {
            return;
        }

        final PaymentStatus newStatus = ChargeStatusMapper.toPaymentStatus(charge.status());
        paymentService.updateStatus(paymentId, newStatus);
        pendingPaymentCheckRepository.deleteById(paymentId);
        log.info("Payment {} finalized as {}", paymentId, newStatus);
    }
}
