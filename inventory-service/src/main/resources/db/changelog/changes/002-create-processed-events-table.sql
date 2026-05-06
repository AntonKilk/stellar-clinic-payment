-- liquibase formatted sql

-- changeset inventory-service:002-create-processed_events-table
CREATE TABLE processed_events
(
    correlation_id      UUID            PRIMARY KEY,
    processed_at        TIMESTAMPTZ     NOT NULL
);

-- rollback DROP TABLE processed_events;