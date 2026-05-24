# Multi-stage build untuk Music Sheet Builder
# Stage 1: Build aplikasi
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper dan konfigurasi
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy package.json untuk frontend dependencies
COPY package.json .
COPY package-lock.json .

# Copy source code
COPY src src

# Build aplikasi dalam production mode
RUN ./gradlew clean build -Pvaadin.productionMode=true --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine

# Install dependencies untuk audio processing (jika diperlukan)
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu

# Create non-root user untuk security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR dari builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create data directory untuk H2 database
RUN mkdir -p /app/data && chown -R spring:spring /app

# Switch ke non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options untuk production
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run aplikasi
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
