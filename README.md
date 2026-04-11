# Stellar CRM

A microservices-based CRM platform for managing private clinic.

## Services

| Service | Port | Description |
|---|---|---|
| [inquiry-service](./inquiry-service) | 8082 | Entry point — manages customer inquiries and initiates the payment flow |
| [customer-service](./customer-service) | 8081 | Stores and manages customer data and contact details |
| [payment-service](./payment-service/README.md) | 8080 | Processes payments via external provider |
| [inventory-service](./inventory-service) | 8083 | Manages booking slots and group capacity limits |

## Architecture

```
                        ┌─────────────────┐
                        │  Inquiry Service │
                        └────────┬────────┘
                                 │ REST
                        ┌────────▼────────┐
                        │ Customer Service │
                        └────────┬────────┘
                                 │ Kafka (payments.requests)
                        ┌────────▼────────┐
                        │ Payment Service  │
                        └────────┬────────┘
                                 │ Kafka (payments.status)
                        ┌────────▼────────┐
                        │Inventory Service │
                        └─────────────────┘
```

Services communicate via REST and Apache Kafka. Each service owns its own PostgreSQL database (database-per-service pattern).

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.x, Spring Data JPA, Spring Web MVC |
| Databases | PostgreSQL 17 (one per service) |
| Migrations | Liquibase |
| Messaging | Apache Kafka |
| Auth | Keycloak 26 (OAuth2 / JWT) |
| Containerization | Docker, Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers |
| Code quality | Checkstyle (Sun Checks), SonarLint |
| Build | Gradle (multi-module) |

## Authentication

All REST endpoints are secured via Keycloak JWT tokens.
The token is passed in the `Authorization: Bearer <token>` header.

Keycloak realm configuration is stored in `keycloak/realm-export.json` and imported automatically on startup.

Available roles: `ADMIN`, `MANAGER`, `TEACHER`, `INTERN`.

## Running Locally

### Prerequisites

- Docker and Docker Compose
- Java 17

### Start each service independently

Each service has its own `docker-compose.yml` that starts PostgreSQL, Keycloak, and the application itself:

```bash
cd inquiry-service
docker-compose up -d
```

For environment variable configuration, see the individual service directory.

### Port reference

| Service | App port | DB port | Keycloak port |
|---|---|---|---|
| payment-service | 8080 | 5432 | 8180 |
| customer-service | 8081 | 5433 | 8180 |
| inquiry-service | 8082 | 5434 | 8180 |
| inventory-service | 8083 | 5435 | 8180 |

## Code Quality

- **Checkstyle** — Sun Checks enforced at build time and in CI
- **SonarLint** — IDE-level static analysis

## CI/CD

GitHub Actions pipelines (`.github/workflows/`):

- **`gradle.yml`** — runs Checkstyle and builds all services on every push to `develop` and `issue-*/**` branches
- **`pr-title-check.yml`** — enforces PR title format: `ISSUE-<number>: <description>`

## Git Conventions

- Baseline branch: `develop`
- Feature branches: `issue-<number>/<short-description>`
- Commit style: present simple tense, starting with uppercase
- One PR = one feature
