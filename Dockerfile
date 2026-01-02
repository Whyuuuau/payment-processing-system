# Dockerfile for Payment Processing System

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom files for dependency caching
COPY pom.xml .
COPY payment-common/pom.xml payment-common/
COPY payment-persistence/pom.xml payment-persistence/
COPY payment-core/pom.xml payment-core/
COPY payment-infrastructure/pom.xml payment-infrastructure/
COPY payment-api/pom.xml payment-api/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY payment-common/src payment-common/src
COPY payment-persistence/src payment-persistence/src
COPY payment-core/src payment-core/src
COPY payment-infrastructure/src payment-infrastructure/src
COPY payment-api/src payment-api/src

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 payment && \
    adduser -D -u 1001 -G payment payment

# Copy JAR from build stage
COPY --from=build /app/payment-api/target/payment-api-*.jar app.jar

# Change ownership
RUN chown -R payment:payment /app

USER payment

# Expose port
EXPOSE 8080

# JVM tuning parameters
ENV JAVA_OPTS="-Xms1G -Xmx2G \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
