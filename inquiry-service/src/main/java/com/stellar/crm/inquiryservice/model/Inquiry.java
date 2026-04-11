package com.stellar.crm.inquiryservice.model;

import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
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
@Table(name = "inquiries")
public class Inquiry {

    private static final int SOURCE_LENGTH = 100;
    private static final int STATUS_LENGTH = 20;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID guid;

    @Column(name = "product_ref_id", nullable = false)
    private UUID productRefId;

    @Column(name = "customer_ref_id", nullable = false)
    private UUID customerRefId;

    @Column(name = "group_ref_id", nullable = true)
    private UUID groupRefId;

    @Column(name = "manager_ref_id", nullable = false)
    private UUID managerRefId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = SOURCE_LENGTH)
    private InquirySource source;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = STATUS_LENGTH)
    private InquiryStatus status;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
