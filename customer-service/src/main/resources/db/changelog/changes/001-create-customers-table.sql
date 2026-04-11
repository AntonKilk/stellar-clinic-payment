-- liquibase formatted sql

-- changeset customer-service:001-create-contact-details-and-customers-tables
CREATE TABLE contact_details
(
    guid                    UUID            PRIMARY KEY,
    email                   VARCHAR(50)     NOT NULL    UNIQUE,
    phone_number            VARCHAR(20),
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL
);

CREATE TABLE customers
(
    guid                    UUID            PRIMARY KEY,
    full_name               VARCHAR(30)     NOT NULL,
    contact_details_id      UUID            NOT NULL,
    created_at              TIMESTAMPTZ     NOT NULL,
    updated_at              TIMESTAMPTZ     NOT NULL,

    CONSTRAINT fk_customers_contact_details
        FOREIGN KEY (contact_details_id)
        REFERENCES contact_details(guid)
);

-- rollback DROP TABLE customers; DROP TABLE contact_details;
