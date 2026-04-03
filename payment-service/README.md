# Payment Service

Microservice responsible for managing payments within the Stellar CRM platform.
It receives payment requests, tracks their statuses, and communicates results back to the Inquiry Service.

## Tech Stack

- Java 17
- Spring Boot 4.x
- Spring Data JPA + Hibernate
- PostgreSQL
- Liquibase
- Kafka
- Keycloak (OAuth2 Resource Server)
- Docker / Docker Compose
- Testcontainers

## Project Structure

```
src/
  main/
    java/com/stellar/crm/paymentservice/
      dto/           # PaymentRequest, PaymentResponse (records)
      model/         # Payment entity, PaymentStatus enum
      repository/    # PaymentRepository, PaymentFilter, PaymentSpecification
      service/       # PaymentService
    resources/
      db/changelog/  # Liquibase migrations
      application.properties
  test/
    java/com/stellar/crm/paymentservice/
      repository/    # PaymentRepositoryTest (Testcontainers)
      service/       # PaymentServiceTest (Mockito)
```

## Payment Status Flow

```
RECEIVED → PENDING → APPROVED
                   → DECLINED
RECEIVED → NOT_SENT
```

## Running Locally

### Prerequisites

- Docker and Docker Compose
- Java 17

### Steps

1. Start infrastructure (PostgreSQL + Keycloak):

```bash
cd payment-service
docker-compose up -d
```

2. Set environment variables in your IDE run configuration
   (see `env.local` for reference values):

```
PAYMENT_DB_URL=jdbc:postgresql://localhost:5432/payment_db
PAYMENT_DB_USERNAME
PAYMENT_DB_PASSWORD
```

3. Run the application:

```bash
./gradlew :payment-service:bootRun
```

Liquibase will automatically apply database migrations on startup.

### Running Tests

```bash
./gradlew :payment-service:test
```

Tests use Testcontainers — Docker must be running.

## Database Migrations

Migrations are managed by Liquibase and located in `src/main/resources/db/changelog/changes/`.

To roll back the last migration:

```bash
./gradlew :payment-service:liquibaseRollback -PliquibaseCommandValue=1
```
