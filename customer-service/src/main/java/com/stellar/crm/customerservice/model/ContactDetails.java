package com.stellar.crm.customerservice.model;

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
@Table(name = "contact_details")
public class ContactDetails {
    private static final int EMAIL_LENGTH = 50;
    private static final int PHONE_LENGTH = 20;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID guid;

    @Column(nullable = false, updatable = false, length = EMAIL_LENGTH)
    private String email;

    @Column(length = PHONE_LENGTH)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
