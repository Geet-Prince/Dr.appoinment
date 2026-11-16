# Stage 1: Build stage
FROM eclipse-temurin:27-jdk-ubi10-minimal AS builder
WORKDIR /workspace

# Copy Maven wrapper and POM for dependency caching
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build application jar
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal Runtime stage
FROM eclipse-temurin:27-jre-ubi10-minimal
WORKDIR /app

# Copy built artifact from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Configuration
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
