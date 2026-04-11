# Stellar CRM

A microservices-based CRM platform for managing customers, inquiries, payments, and inventory.

## Services

| Service | Description |
|---|---|
| [customer-service](./customer-service/README.md) | Stores and manages customer data |
| [inquiry-service](./inquiry-service/README.md) | Manages customer inquiries and initiates payment flow |
| [payment-service](./payment-service/README.md) | Processes payments via external provider |
| [inventory-service](./inventory-service/README.md) | Manages booking slots and group limits |

## Architecture

```
                        ┌─────────────────┐
                        │  Inquiry Service │
                        └────────┬────────┘
                                 │ REST
                        ┌────────▼────────┐
                        │ Customer Service │
                        └─────────────────┘
                                 │ Kafka (payments.requests)
                        ┌────────▼────────┐
                        │ Payment Service  │
                        └────────┬────────┘
                                 │ Kafka (payments.status)
                        ┌────────▼────────┐
                        │Inventory Service │
                        └─────────────────┘
```

Services communicate via REST and Apache Kafka. Each service has its own PostgreSQL database.

## Tech Stack

- Java 17
- Spring Boot 4.x
- PostgreSQL
- Liquibase
- Apache Kafka
- Keycloak 26 (OAuth2 / JWT)
- Docker / Docker Compose
- Testcontainers

## Authentication

All REST endpoints are secured via Keycloak JWT tokens.
Token is passed in the `Authorization: Bearer <token>` header.

Keycloak realm configuration is stored in `keycloak/realm-export.json` and imported automatically on startup.

Available roles: `ADMIN`, `MANAGER`, `TEACHER`, `INTERN`.

## Running Locally

### Prerequisites

- Docker and Docker Compose
- Java 17

### Start each service independently

Each service has its own `docker-compose.yml` that starts PostgreSQL and Keycloak:

```bash
cd payment-service
docker-compose up -d
```

Then run the service — see the individual service README for environment variable setup.

## Code Quality

- Checkstyle (Sun Checks)
- SonarLint

## CI/CD

GitHub Actions pipeline enforces:
- PR title format: `^[A-Z][a-zA-Z0-9 \-]+(: ).+$`
- Build and lint on every push

## Git Conventions

- Baseline branch: `develop`
- Feature branches: `issue-*/*`
- Commit style: present simple, starting with uppercase
- One PR = one feature