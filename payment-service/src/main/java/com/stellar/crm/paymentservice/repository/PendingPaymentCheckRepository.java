package com.stellar.crm.paymentservice.repository;

import com.stellar.crm.paymentservice.model.PendingPaymentCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PendingPaymentCheckRepository extends JpaRepository<PendingPaymentCheck, UUID> {
}
