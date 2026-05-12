package com.stellar.crm.paymentservice.model;

import com.stellar.crm.contracts.payment.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    private static final int AMOUNT_PRECISION = 5;
    private static final int AMOUNT_SCALE = 2;
    private static final int CURRENCY_LENGTH = 3;
    private static final int STATUS_LENGTH = 20;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "inquiry_ref_id", nullable = false)
    private UUID inquiryRefId;

    @Column(nullable = false, precision = AMOUNT_PRECISION, scale = AMOUNT_SCALE)
    private BigDecimal amount;

    @Column(nullable = false, length = CURRENCY_LENGTH)
    private String currency;

    @Column(name = "transaction_ref_id")
    private UUID transactionRefId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = STATUS_LENGTH)
    private PaymentStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
