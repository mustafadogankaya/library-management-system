# Library Management System

A modern web-based library management system built with Spring Boot and featuring a clean, responsive user interface.

## 🚀 Features

- **Book Management**: Add, search, update, and delete books
- **Modern UI**: Clean and responsive web interface
- **RESTful API**: Well-structured REST endpoints
- **Data Persistence**: JSON-based data storage
- **Spring Security**: Secure authentication and authorization
- **Responsive Design**: Works on desktop and mobile devices

## 🛠️ Technology Stack

- **Backend**: Spring Boot 2.7.18
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Build Tool**: Maven
- **Java Version**: 11+
- **Data Storage**: JSON files
- **Security**: Spring Security
- **Testing**: JUnit 5

## 📋 Prerequisites

Before running this application, make sure you have the following installed:

- Java 11 or higher
- Maven 3.6 or higher
- Git

## 🏃‍♂️ Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/YOUR_USERNAME/library-management-system.git
cd library-management-system
```

### 2. Run with Docker (Recommended)
```bash
# Build and start the application with docker compose
docker compose up --build

# Or run in background
docker compose up --build -d
```

The application will be available at: `http://localhost:8080`

### 3. Alternative: Build and run locally

#### Build the project
```bash
mvn clean install
```

#### Run the application
```bash
mvn spring-boot:run
```

#### Access the application
Open your browser and navigate to: `http://localhost:8080`

## 🔧 Alternative Running Methods

### Using Java JAR
```bash
# Build the JAR file
mvn clean package

# Run the JAR
java -jar target/library-management-web-1.0-SNAPSHOT.jar
```

### Development Mode (with auto-reload)
```bash
mvn spring-boot:run -Dspring-boot.run.fork=false
```

## 🐳 Docker Setup

This application is fully containerized and can be run using Docker.

### Prerequisites
- Docker
- Docker Compose

### Quick Start with Docker
```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/library-management-system.git
cd library-management-system

# Build and start with docker compose
docker compose up --build
```

### Docker Commands

#### Start the application
```bash
docker compose up --build
```

#### Start in background
```bash
docker compose up --build -d
```

#### Stop the application
```bash
docker compose down
```

#### View logs
```bash
docker compose logs -f
```

#### Remove volumes (reset data)
```bash
docker compose down -v
```

### Docker Configuration

The application uses the following Docker setup:

- **Dockerfile**: Multi-stage build using Eclipse Temurin JRE 17 Alpine
- **docker-compose.yml**: Orchestrates the application with persistent volumes
- **Data Persistence**: Application data is stored in Docker volumes
- **Health Checks**: Built-in health monitoring via Spring Boot Actuator
- **Environment**: Uses `docker` Spring profile for container-specific configuration

### Available Services

- **Application**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Application Info**: `http://localhost:8080/actuator/info`

### Data Persistence

The application data is automatically persisted using Docker volumes:
- `library_data`: Stores the JSON database file
- `library_logs`: Stores application logs

### Environment Variables

You can customize the application by setting environment variables in `docker-compose.yml`:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=docker
  - JAVA_OPTS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

## 📚 API Endpoints

### Books API
- `GET /api/books` - Get all books
- `GET /api/books/{id}` - Get book by ID
- `POST /api/books` - Add new book
- `PUT /api/books/{id}` - Update book
- `DELETE /api/books/{id}` - Delete book
- `GET /api/books/search?title={title}` - Search books by title

### Authentication
- `POST /api/login` - User login
- `POST /api/register` - User registration
- `POST /api/logout` - User logout

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/librarysystem/
│   │       ├── app/                    # Main application class
│   │       ├── controller/             # REST controllers
│   │       ├── service/                # Business logic
│   │       ├── model/                  # Data models
│   │       ├── storage/                # Data access layer
│   │       ├── security/               # Security configuration
│   │       └── exception/              # Custom exceptions
│   └── resources/
│       ├── static/                     # CSS, JS, HTML files
│       └── application.properties      # Configuration
└── test/                               # Unit tests
```

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## 📦 Building for Production

```bash
# Create production JAR
mvn clean package -Pprod

# The JAR file will be created in target/ directory
```

## 🔒 Security Features

- User authentication and authorization
- Password encryption
- Session management
- CSRF protection
- Secure HTTP headers

## 🌟 Key Features Explained

### Book Management
- Add new books with title, author, publication year, and ISBN
- Search functionality with multiple criteria
- Update existing book information
- Delete books from the system
- View detailed book information

### User Interface
- Clean, modern design
- Responsive layout for all devices
- Intuitive navigation
- Real-time form validation
- Loading states and feedback

### Data Storage
- JSON-based file storage for simplicity
- Automatic data persistence
- Data integrity validation
- Backup and recovery options

## 🔄 Development Workflow

1. **Setup**: Clone the repo and install dependencies
2. **Development**: Make changes and test locally
3. **Testing**: Run unit tests and integration tests
4. **Build**: Create production-ready JAR
5. **Deploy**: Deploy to your preferred platform

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

If you have any questions or issues, please:

1. Check the [Issues](https://github.com/YOUR_USERNAME/library-management-system/issues) page
2. Create a new issue if your problem isn't already reported
3. Provide as much detail as possible including error messages and steps to reproduce

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Maven for dependency management
- All contributors and testers

---

**Happy coding! 🎉**
