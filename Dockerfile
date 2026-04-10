# Build stage for frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
# outDir is '../src/main/resources/static/admin' in vite.config.ts
RUN npm run build

# Build stage for backend
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Install gcompat and libc6-compat for libc compatibility on Alpine
RUN apk add --no-cache gcompat libc6-compat

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY config config

# Grant execute permission
RUN chmod +x ./gradlew

# Download dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src
COPY frontend frontend

# Copy built frontend assets to the static resources directory before JAR build
# Since vite.config.ts has outDir: '../src/main/resources/static/admin'
# and frontend-build stage WORKDIR is /app/frontend, the assets are at /app/src/main/resources/static/admin
COPY --from=frontend-build /app/src/main/resources/static/admin src/main/resources/static/admin

# Verify frontend assets exist
RUN ls -la src/main/resources/static/admin

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
