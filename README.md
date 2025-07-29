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

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

### 4. Access the application
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

### Using Docker
```bash
# Build the application
mvn clean package -DskipTests

# Build Docker image
docker build -t library-management-system .

# Run with Docker
docker run -p 8080:8080 library-management-system
```

For detailed Docker instructions, see [README-Docker.md](README-Docker.md).

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
