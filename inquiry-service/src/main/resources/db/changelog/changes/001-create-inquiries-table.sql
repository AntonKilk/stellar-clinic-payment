-- liquibase formatted sql

-- changeset inquiry-service:001-create-inquiries-table
CREATE TABLE inquiries
(
    guid                    UUID            PRIMARY KEY,
    product_ref_id          UUID            NOT NULL,
    customer_ref_id         UUID            NOT NULL,
    group_ref_id            UUID,
    manager_ref_id          UUID            NOT NULL,
    source                  VARCHAR(100)    NOT NULL,
    comment                 text,
    status                  VARCHAR(20)     NOT NULL,
    note                    text,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

-- rollback DROP TABLE inquiries;