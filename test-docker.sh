#!/bin/bash
# Docker Test Script for Library Management System

echo "🐳 Testing Library Management System Docker Setup"
echo "================================================="

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed or not available in PATH"
    exit 1
fi

# Build the application locally first
echo "📦 Building application with Maven..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Maven build failed"
    exit 1
fi

echo "✅ Maven build successful"

# Build Docker image
echo "🔨 Building Docker image..."
docker build -t library-management-test .

if [ $? -ne 0 ]; then
    echo "❌ Docker build failed"
    exit 1
fi

echo "✅ Docker image built successfully"

# Start services with docker compose
echo "🚀 Starting services with Docker Compose..."
docker compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Docker Compose failed to start services"
    exit 1
fi

echo "✅ Services started successfully"

# Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 10

# Test health endpoint
echo "🔍 Testing health endpoint..."
health_response=$(curl -s http://localhost:8080/actuator/health)
if [[ $health_response == *"UP"* ]]; then
    echo "✅ Health check passed: $health_response"
else
    echo "❌ Health check failed: $health_response"
    docker compose logs
    docker compose down
    exit 1
fi

# Test main application
echo "🔍 Testing main application..."
main_response=$(curl -s http://localhost:8080/ | head -5)
if [[ $main_response == *"Kütüphane Yönetim Sistemi"* ]]; then
    echo "✅ Main application is responding correctly"
else
    echo "❌ Main application test failed"
    docker compose logs
    docker compose down
    exit 1
fi

echo ""
echo "🎉 All tests passed! The application is running successfully."
echo "📖 Access the application at: http://localhost:8080"
echo "🏥 Health check at: http://localhost:8080/actuator/health"
echo ""
echo "To stop the application, run: docker compose down"
echo "To view logs, run: docker compose logs -f"