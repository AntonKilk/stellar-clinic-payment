package com.stellar.crm.paymentservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pending_payment_check")
public class PendingPaymentCheck {

    @Id
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "inquiry_ref_id", nullable = false, updatable = false)
    private UUID inquiryRefId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
