# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY contracts/ contracts/

COPY payment-service/gradlew payment-service/
COPY payment-service/gradle/ payment-service/gradle/
COPY payment-service/build.gradle payment-service/settings.gradle payment-service/
COPY payment-service/src/ payment-service/src/

WORKDIR /workspace/payment-service

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/payment-service/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080
EXPOSE 8443
EXPOSE 5005

ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-jar", "app.jar"]
