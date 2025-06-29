# PowerShell Docker Test Script for Library Management System

Write-Host "🐳 Testing Library Management System Docker Setup" -ForegroundColor Blue
Write-Host "=================================================" -ForegroundColor Blue

# Check if Docker is available
try {
    docker --version | Out-Null
    Write-Host "✅ Docker is available" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not installed or not available in PATH" -ForegroundColor Red
    exit 1
}

# Build the application locally first
Write-Host "📦 Building application with Maven..." -ForegroundColor Yellow
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Maven build successful" -ForegroundColor Green

# Build Docker image
Write-Host "🔨 Building Docker image..." -ForegroundColor Yellow
docker build -t library-management-test .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker image built successfully" -ForegroundColor Green

# Start services with docker compose
Write-Host "🚀 Starting services with Docker Compose..." -ForegroundColor Yellow
docker compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker Compose failed to start services" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Services started successfully" -ForegroundColor Green

# Wait for application to start
Write-Host "⏳ Waiting for application to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Test health endpoint
Write-Host "🔍 Testing health endpoint..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
    if ($healthResponse.status -eq "UP") {
        Write-Host "✅ Health check passed: $($healthResponse | ConvertTo-Json)" -ForegroundColor Green
    } else {
        Write-Host "❌ Health check failed: $($healthResponse | ConvertTo-Json)" -ForegroundColor Red
        docker compose logs
        docker compose down
        exit 1
    }
} catch {
    Write-Host "❌ Health check failed: $($_.Exception.Message)" -ForegroundColor Red
    docker compose logs
    docker compose down
    exit 1
}

# Test main application
Write-Host "🔍 Testing main application..." -ForegroundColor Yellow
try {
    $mainResponse = Invoke-WebRequest -Uri "http://localhost:8080/" -Method Get
    if ($mainResponse.Content -like "*Kütüphane Yönetim Sistemi*") {
        Write-Host "✅ Main application is responding correctly" -ForegroundColor Green
    } else {
        Write-Host "❌ Main application test failed" -ForegroundColor Red
        docker compose logs
        docker compose down
        exit 1
    }
} catch {
    Write-Host "❌ Main application test failed: $($_.Exception.Message)" -ForegroundColor Red
    docker compose logs
    docker compose down
    exit 1
}

Write-Host ""
Write-Host "🎉 All tests passed! The application is running successfully." -ForegroundColor Green
Write-Host "📖 Access the application at: http://localhost:8080" -ForegroundColor Cyan
Write-Host "🏥 Health check at: http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host ""
Write-Host "To stop the application, run: docker compose down" -ForegroundColor Yellow
Write-Host "To view logs, run: docker compose logs -f" -ForegroundColor Yellow