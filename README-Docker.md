# Docker Deployment Guide

This guide explains how to build and run the Library Management System using Docker.

## Prerequisites

- Docker installed on your system
- Java 11 and Maven (for building the JAR file)

## Building and Running with Docker

### Step 1: Build the Application

First, build the Spring Boot JAR file:

```bash
mvn clean package -DskipTests
```

### Step 2: Build Docker Image

Build the Docker image:

```bash
docker build -t library-management-system .
```

### Step 3: Run the Container

Run the application in a Docker container:

```bash
docker run -p 8080:8080 library-management-system
```

The application will be available at: `http://localhost:8080`

### Step 4: Run with Data Persistence (Optional)

To persist data between container restarts, mount a volume for the data directory:

```bash
docker run -p 8080:8080 -v $(pwd)/data:/app/data library-management-system
```

## Docker Configuration

### Environment Variables

The Dockerfile sets the following environment variables:
- `SPRING_PROFILES_ACTIVE=docker` - Uses Docker-specific configuration
- `JAVA_OPTS` - JVM optimization for containerized environment

### Ports

- **Port 8080**: Web application and REST API

### Volumes

- `/app/data` - Directory for JSON data storage

### Docker Image Details

- **Base Image**: `openjdk:11-jre-slim`
- **Working Directory**: `/app`
- **JAR File**: `app.jar`
- **Image Size**: ~350MB (approximately)

## Useful Docker Commands

### View Running Containers
```bash
docker ps
```

### Stop Container
```bash
docker stop <container_id>
```

### View Logs
```bash
docker logs <container_id>
```

### Remove Container
```bash
docker rm <container_id>
```

### Remove Image
```bash
docker rmi library-management-system
```

### Build and Run in One Command
```bash
mvn clean package -DskipTests && docker build -t library-management-system . && docker run -p 8080:8080 library-management-system
```

## Production Deployment

For production deployment, consider:

1. **Environment Variables**: Override configuration using environment variables
2. **Data Persistence**: Use Docker volumes or external storage
3. **Health Checks**: Monitor application health
4. **Resource Limits**: Set memory and CPU limits
5. **Security**: Use non-root user and security scanning

### Production Run Example
```bash
docker run -d \
  --name library-management \
  -p 8080:8080 \
  -v /host/data:/app/data \
  --restart unless-stopped \
  --memory 1g \
  --cpus 0.5 \
  library-management-system
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**: Change the host port mapping `-p 8081:8080`
2. **Permission Issues**: Ensure data directory has proper permissions
3. **Memory Issues**: Adjust JAVA_OPTS or container memory limits

### Debug Mode

Run with debug output:
```bash
docker run -p 8080:8080 -e JAVA_OPTS="-Xmx512m -Xdebug" library-management-system
```

## API Endpoints

Once running, the following endpoints are available:

- **Web Interface**: `http://localhost:8080/`
- **Books API**: `http://localhost:8080/api/books`
- **Health Check**: `http://localhost:8080/` (returns 200 OK)

For complete API documentation, refer to the main README.md file.