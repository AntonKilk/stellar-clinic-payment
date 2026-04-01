-- liquibase formatted sql

-- changeset payment-service:001-create-payments-table
CREATE TABLE payments
(
    id               UUID           PRIMARY KEY,
    inquiry_ref_id   UUID           NOT NULL,
    amount           NUMERIC(5, 2)  NOT NULL,
    currency         VARCHAR(3)     NOT NULL,
    transaction_ref_id UUID         NULL,
    status           VARCHAR(20)   NOT NULL,
    note             TEXT           NULL,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL
);

-- rollback DROP TABLE payments;