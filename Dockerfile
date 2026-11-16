FROM eclipse-temurin:27-jre-ubi10-minimal
WORKDIR /app

# Copy built application jar
COPY target/*.jar app.jar

# Configuration
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseZGC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
