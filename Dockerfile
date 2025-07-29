# Use OpenJDK 11 JRE for runtime (smaller than JDK)
FROM openjdk:11-jre-slim

# Set metadata
LABEL maintainer="Library Management System"
LABEL description="Spring Boot Library Management System"
LABEL version="1.0"

# Create application directory
WORKDIR /app

# Create data directory for JSON storage
RUN mkdir -p /app/data

# Copy the Spring Boot JAR file
COPY target/library-management-web-1.0-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Set Spring profile to docker
ENV SPRING_PROFILES_ACTIVE=docker

# Configure JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]