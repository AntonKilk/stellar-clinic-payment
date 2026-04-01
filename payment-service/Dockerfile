# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY payment-service/gradlew .
COPY payment-service/gradle/ gradle/
COPY payment-service/build.gradle .
COPY payment-service/settings.gradle .
COPY payment-service/src/ src/

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080
EXPOSE 8443
EXPOSE 5005

ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-jar", "app.jar"]
