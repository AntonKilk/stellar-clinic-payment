-- liquibase formatted sql

-- changeset inventory-service:001-create-groups-table
CREATE TABLE groups
(
    guid                    UUID            PRIMARY KEY,
    group_ref_id            UUID            NOT NULL UNIQUE,
    current_count           integer         NOT NULL DEFAULT 0,
    group_limit             integer         NOT NULL,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

-- rollback DROP TABLE groups;