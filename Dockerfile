# Build stage
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY gradle.properties gradle.properties
COPY build.gradle .
COPY settings.gradle .

# Grant execute permission
RUN chmod +x ./gradlew

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application
RUN ./gradlew clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user
RUN addgroup -S ticketscale && adduser -S ticketscale -G ticketscale

# Copy built artifact from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R ticketscale:ticketscale /app

# Switch to non-root user
USER ticketscale

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# JVM optimizations for containerized environments
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
