# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

COPY customer-service/gradlew .
COPY customer-service/gradle/ gradle/
COPY customer-service/build.gradle .
COPY customer-service/settings.gradle .
COPY customer-service/src/ src/

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER appuser

# HTTP (non-TLS)
EXPOSE 8080
# HTTPS (TLS)
EXPOSE 8443
# JDWP remote debug
EXPOSE 5005

ENTRYPOINT ["java", \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", \
    "-jar", "app.jar"]
