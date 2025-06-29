# Docker Setup Guide

Bu kılavuz, Library Management System uygulamasının Docker ile çalıştırılması için gerekli tüm bilgileri içermektedir.

## Gereksinimler

- Docker (20.10+)
- Docker Compose (v2.0+)
- Git

## Hızlı Başlangıç

### 1. Projeyi Klonlayın
```bash
git clone https://github.com/mustafadogankaya/library-management-system.git
cd library-management-system
```

### 2. Uygulamayı Başlatın
```bash
# Tek komutla uygulamayı build edip başlatın
docker compose up --build

# Veya arka planda çalıştırmak için
docker compose up --build -d
```

### 3. Uygulamaya Erişin
- Ana uygulama: http://localhost:8080
- Health check: http://localhost:8080/actuator/health
- Uygulama bilgileri: http://localhost:8080/actuator/info

## Docker Komutları

### Temel Komutlar
```bash
# Uygulamayı başlat
docker compose up --build

# Uygulamayı arka planda başlat
docker compose up --build -d

# Uygulamayı durdur
docker compose down

# Logları görüntüle
docker compose logs -f

# Servislerin durumunu kontrol et
docker compose ps

# Volumeleri de silerek tamamen temizle
docker compose down -v
```

### İleri Düzey Komutlar
```bash
# Sadece Docker image'ını build et
docker build -t library-management .

# Containerın içine gir
docker compose exec library-app sh

# Volumeleri listele
docker volume ls

# Belirli bir servisin loglarını izle
docker compose logs -f library-app

# Servisleri yeniden başlat
docker compose restart

# Belirli bir servisi yeniden build et
docker compose build library-app
```

## Dosya Yapısı

### Dockerfile
Uygulama için minimum boyutlu, güvenli bir container image'ı oluşturur:
- **Base Image**: Eclipse Temurin 17 JRE Alpine
- **Build Process**: Pre-compiled JAR dosyasını kopyalar
- **Security**: Non-privileged user ile çalışır
- **Optimization**: JVM container optimizasyonları

### docker-compose.yml
Tam bir çalışma ortamı tanımlar:
- **Application Service**: Ana Spring Boot uygulaması
- **Volumes**: Veri kalıcılığı için
- **Networks**: Güvenli container iletişimi
- **Environment Variables**: Yapılandırma yönetimi

### .dockerignore
Gereksiz dosyaların image'a dahil edilmesini engeller:
- IDE dosyaları
- Git repository
- Test dosyaları
- Geliştirme araçları

## Veri Kalıcılığı

Uygulama verileri Docker volume'ları kullanılarak kalıcı hale getirilir:

### Volumes
- `library_data`: JSON veritabanı dosyası (`/app/data`)
- `library_logs`: Uygulama log dosyaları (`/app/logs`)

### Veri Yedekleme
```bash
# Veri volume'unun yedeğini al
docker run --rm -v library-management-system_library_data:/data -v $(pwd):/backup alpine tar czf /backup/library-data-backup.tar.gz -C /data .

# Yedeği geri yükle
docker run --rm -v library-management-system_library_data:/data -v $(pwd):/backup alpine tar xzf /backup/library-data-backup.tar.gz -C /data
```

## Ortam Değişkenleri

docker-compose.yml dosyasında özelleştirilebilir ortam değişkenleri:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - JAVA_OPTS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
  - SERVER_PORT=8080
  - LOGGING_LEVEL_ROOT=INFO
```

## Ağ Yapılandırması

### Portlar
- **8080**: Ana uygulama portu (HTTP)

### Ağlar
- `library-network`: Container'lar arası güvenli iletişim

## Güvenlik

### Güvenlik Özellikleri
- Minimal base image (Alpine Linux)
- Non-root user ile çalışma
- Gereksiz paketlerin kaldırılması
- Güvenli varsayılan yapılandırma

### Best Practices
- Secret bilgiler environment variable'lar ile yönetilir
- Container içinde minimum yetki prensibi
- Regular security updates

## Troubleshooting

### Yaygın Sorunlar ve Çözümleri

#### Port zaten kullanımda
```bash
# 8080 portunu kullanan process'i bul
lsof -i :8080

# Veya farklı bir port kullan
sed -i 's/8080:8080/8081:8080/g' docker-compose.yml
```

#### Container başlamıyor
```bash
# Detaylı logları kontrol et
docker compose logs library-app

# Container'ın durumunu kontrol et
docker compose ps
```

#### Veri kaybı
```bash
# Volume'ların durumunu kontrol et
docker volume ls
docker volume inspect library-management-system_library_data
```

#### Performans sorunları
```bash
# Resource kullanımını izle
docker stats

# Memory limitlerini artır
# docker-compose.yml'de deploy section ekle:
deploy:
  resources:
    limits:
      memory: 1G
    reservations:
      memory: 512M
```

## Test Scripti

Otomatik test için hazır scriptler:

### Linux/macOS
```bash
./test-docker.sh
```

### Windows (PowerShell)
```powershell
.\test-docker.ps1
```

Bu scriptler:
- Maven build kontrolü
- Docker image build
- Container başlatma
- Health check testi
- Ana uygulama testi

## Geliştirme Ortamı

### Development Mode
Geliştirme sırasında kod değişikliklerini anında görmek için:

```yaml
# docker-compose.override.yml oluştur
version: '3.8'
services:
  library-app:
    volumes:
      - ./src:/app/src:ro
      - ./target:/app/target
    environment:
      - SPRING_DEVTOOLS_RESTART_ENABLED=true
```

### Debugging
Container içinde debug yapmak için:

```bash
# Debug portu ile başlat
docker compose run --rm -p 5005:5005 library-app java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar app.jar
```

## İzleme ve Loglama

### Log Yönetimi
```bash
# Tüm logları izle
docker compose logs -f

# Sadece hata loglarını göster
docker compose logs library-app | grep ERROR

# Log rotation için
docker compose config | grep -A 5 logging
```

### Monitoring
Health check endpoint'i üzerinden monitoring:
```bash
# Basit health check
curl http://localhost:8080/actuator/health

# Detaylı sistem bilgileri
curl http://localhost:8080/actuator/info
```

## Üretim Ortamı

### Production Deployment
Üretim ortamı için ek yapılandırmalar:

```yaml
# docker-compose.prod.yml
services:
  library-app:
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
      restart_policy:
        condition: on-failure
        max_attempts: 3
    environment:
      - SPRING_PROFILES_ACTIVE=production
```

### Scaling
```bash
# Uygulama instance'larını çoğalt
docker compose up --scale library-app=3
```

Bu doküman, Docker ile Library Management System'in tüm yönlerini kapsamaktadır. Daha fazla bilgi için [Docker Documentation](https://docs.docker.com/) sayfasını ziyaret edebilirsiniz.