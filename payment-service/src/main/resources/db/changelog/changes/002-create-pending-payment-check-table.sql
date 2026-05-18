-- liquibase formatted sql

-- changeset payment-service:002-create-pending-payment-check-table
CREATE TABLE pending_payment_check
(
    payment_id       UUID           PRIMARY KEY,
    inquiry_ref_id   UUID           NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL
);

-- rollback DROP TABLE pending_payment_check;