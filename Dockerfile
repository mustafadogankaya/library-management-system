# Simple Dockerfile for Spring Boot Library Management System
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Create data directory
RUN mkdir -p /app/data

# Copy the pre-built JAR file
COPY target/library-management-web-*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]