# TODO App

<div align="center">

**🚀 A modern, enterprise-grade TODO application built with Spring Boot**

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.5+-blue.svg)](https://gradle.org/)
[![H2](https://img.shields.io/badge/Database-H2-blue.svg)](https://www.h2database.com/)
[![Tests](https://img.shields.io/badge/Tests-183-success.svg)](#testing)

*Full-stack multi-list TODO management with REST API, server-rendered views, and comprehensive testing*

[🚀 Quick Start](#quick-start) • [📚 Documentation](#documentation) • [🧪 Testing](#testing) • [🏗️ Architecture](#architecture)

</div>

---

## 🎯 What Is This?

A **production-ready TODO application** demonstrating modern Spring Boot development practices:

- **📋 Multi-List Management**: Create and organize multiple TODO lists
- **✅ Full CRUD Operations**: Add, edit, complete, and delete items
- **🌐 Dual Interface**: Both REST API and server-rendered web UI
- **🧪 Test-Driven Development**: 100% test coverage with TDD approach
- **🛡️ Enterprise Security**: Security headers, CORS, error handling
- **⚡ Performance Optimized**: Sub-5-second test suite target

### **Perfect For:**
- **👨‍💻 Developer Portfolio**: Showcase modern Spring Boot skills
- **🎓 Learning Reference**: Study enterprise development patterns
- **🏗️ Project Template**: Bootstrap new Spring Boot applications
- **📋 Interview Preparation**: Demonstrate full-stack capabilities

---

## 🚀 Quick Start

### **Prerequisites**
- Java 17+ (tested with Java 19)
- Git (for cloning)

### **Get Running in 2 Minutes**

```bash
# 1. Clone the repository
git clone <repository-url>
cd todo-app

# 2. Run tests (verify setup)
./gradlew test      # Linux/Mac
.\gradlew test      # Windows

# 3. Start the application
./gradlew bootRun   # Linux/Mac  
.\gradlew bootRun   # Windows

# 4. Open your browser
open http://localhost:8080
```

### **Instant Access**
- **🌐 Web Application**: http://localhost:8080
- **📡 REST API**: http://localhost:8080/api  
- **🗄️ Database Console**: http://localhost:8080/h2-console

---

## ✨ Features

### **🎨 User Interface**
- ✅ **Responsive Design** - Works on desktop, tablet, and mobile
- ✅ **Accessible** - Semantic HTML with ARIA labels
- ✅ **Modern CSS** - Clean, professional styling
- ✅ **Interactive** - Real-time updates and visual feedback

### **🔌 REST API**
- ✅ **OpenAPI 3.0 Specification** - Complete API documentation
- ✅ **RESTful Design** - Standard HTTP methods and status codes
- ✅ **JSON Responses** - Consistent data format
- ✅ **Error Handling** - Detailed error messages and codes

### **🛡️ Enterprise Security**
- ✅ **Security Headers** - CSP, HSTS, X-Frame-Options, etc.
- ✅ **CORS Configuration** - Cross-origin request support
- ✅ **Input Validation** - Server-side validation with clear error messages
- ✅ **Safe Defaults** - Security-first configuration

### **📊 Monitoring & Observability**
- ✅ **Request Logging** - Structured logs with correlation IDs
- ✅ **Performance Monitoring** - Response time tracking
- ✅ **Health Checks** - Application status endpoints
- ✅ **Database Metrics** - Connection pool and query performance

### **🧪 Quality Assurance**
- ✅ **100% Test Coverage** - Unit, integration, and contract tests
- ✅ **TDD Approach** - Test-first development methodology
- ✅ **Performance Tests** - Automated performance regression detection
- ✅ **Code Quality** - Checkstyle, Spotless formatting

---

## 📚 Documentation

### **📖 Getting Started**
- [**🚀 Quickstart Guide**](specs/001-title-multi-list/quickstart.md) - Get up and running in 5 minutes
- [**📋 Feature Specification**](specs/001-title-multi-list/spec.md) - Complete feature requirements
- [**🏗️ Architecture Plan**](specs/001-title-multi-list/plan.md) - Technical architecture and design decisions

### **🔧 Development**
- [**📊 API Documentation**](specs/001-title-multi-list/contracts/openapi.yaml) - OpenAPI specification
- [**⚡ Performance Guide**](src/test/java/com/todoapp/perf/TestSuitePerformanceReport.md) - Optimization strategies
- [**🧪 Testing Strategy**](#testing) - Comprehensive testing approach

### **🗂️ Project Structure**
```
src/
├── main/java/com/todoapp/
│   ├── 🚀 Application.java              # Spring Boot entry point
│   ├── ⚙️  config/                      # Configuration classes
│   │   ├── DatabaseConfig.java         # DB verification & monitoring
│   │   ├── LoggingConfig.java          # Request/response logging
│   │   └── SecurityConfig.java         # Security headers & CORS
│   ├── 🏢 domain/                       # Domain models & business rules
│   │   ├── TodoList.java               # List entity with validation
│   │   └── TodoItem.java               # Item entity with business logic
│   ├── 💾 persistence/                  # Data access layer
│   │   ├── TodoListRepository.java     # List data operations
│   │   └── TodoItemRepository.java     # Item data operations
│   ├── 🔧 service/                      # Business logic layer
│   │   ├── TodoListService.java        # List business operations
│   │   └── TodoItemService.java        # Item business operations
│   └── 🌐 web/                          # Presentation layer
│       ├── ListsController.java        # Lists REST endpoints
│       ├── ItemsController.java        # Items REST endpoints
│       ├── ViewController.java         # Server-rendered pages
│       ├── HomeController.java         # Root routing
│       └── ErrorHandler.java           # Global exception handling
├── main/resources/
│   ├── ⚙️  application.yml              # Application configuration
│   ├── 🗄️ db/migration/                 # Database schema evolution
│   └── 🎨 templates/                    # Thymeleaf HTML templates
└── test/java/com/todoapp/        # Comprehensive test suite
    ├── 🧪 unit/                         # Fast, isolated unit tests
    ├── 🔗 integration/                  # End-to-end integration tests  
    ├── 📋 contract/                     # API contract verification
    └── ⚡ perf/                         # Performance & load tests
```

---

## 🧪 Testing

### **📈 Test Pyramid Implementation**

```
    🔺 Contract Tests (10)     ← API behavior verification
   🔺🔺 Integration Tests (25)  ← End-to-end user workflows  
  🔺🔺🔺 Unit Tests (150+)      ← Fast, isolated business logic
```

### **🏃‍♂️ Running Tests**

```bash
# All tests (target: <5 seconds)
./gradlew test

# Test categories
./gradlew test --tests="*unit*"         # Unit tests only
./gradlew test --tests="*integration*"  # Integration tests
./gradlew test --tests="*contract*"     # Contract tests
./gradlew test --tests="*perf*"        # Performance tests

# Test reports
./gradlew test
open build/reports/tests/test/index.html
```

### **🎯 Test Coverage**

| Layer | Tests | Coverage | Speed |
|-------|-------|----------|-------|
| **Domain** | 45+ tests | 100% | <1s |
| **Service** | 35+ tests | 100% | <1s |
| **Web** | 40+ tests | 100% | <2s |
| **Integration** | 25+ tests | E2E | <2s |
| **Contract** | 10+ tests | API | <1s |
| **Total** | **183+ tests** | **100%** | **<5s** |

### **🔧 Test-Driven Development**

Every feature follows the **Red-Green-Refactor** cycle:

1. **🔴 Red**: Write failing test that describes desired behavior
2. **🟢 Green**: Write minimal code to make the test pass
3. **🔵 Refactor**: Improve code while keeping tests green

---

## 🏗️ Architecture

### **🎯 Design Principles**

- **🧱 Domain-Driven Design**: Clear separation of business logic
- **🔄 SOLID Principles**: Maintainable, extensible code structure
- **⚡ Performance First**: Optimized for speed and efficiency  
- **🛡️ Security by Default**: Safe, secure configuration out-of-the-box
- **🧪 Testability**: Every component designed for easy testing

### **📊 Technical Stack**

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | Spring Boot 3.2.0 | Application foundation |
| **Database** | H2 (file-based) | Data persistence |
| **Migration** | Flyway | Database schema management |
| **Security** | Spring Security 6.x | Security headers & CORS |
| **Testing** | JUnit 5 + MockMvc | Comprehensive test suite |
| **Build** | Gradle 8.5+ | Dependency management & automation |
| **Templates** | Thymeleaf | Server-side rendering |
| **Logging** | SLF4J + Logback | Structured logging |

### **🔄 Data Flow**

```
🌐 HTTP Request
    ↓
🛡️ Security Filter
    ↓  
📊 Request Logging
    ↓
🎮 Controller Layer
    ↓
🔧 Service Layer (Business Logic)
    ↓
💾 Repository Layer (Data Access)
    ↓
🗄️ H2 Database
```

---

## 🚀 API Reference

### **📋 Lists API**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/lists` | Get all lists |
| `POST` | `/api/lists` | Create new list |
| `GET` | `/api/lists/{id}` | Get specific list |
| `PATCH` | `/api/lists/{id}` | Update list name |
| `DELETE` | `/api/lists/{id}` | Delete list |

### **✅ Items API**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/lists/{listId}/items` | Get items in list |
| `POST` | `/api/lists/{listId}/items` | Create new item |
| `GET` | `/api/items/{id}` | Get specific item |
| `PATCH` | `/api/items/{id}` | Update item text |
| `PATCH` | `/api/items/{id}/toggle` | Toggle completion |
| `DELETE` | `/api/items/{id}` | Delete item |

### **📊 Example Requests**

<details>
<summary><strong>🔍 Click to expand API examples</strong></summary>

```bash
# Create a list
curl -X POST http://localhost:8080/api/lists \
  -H "Content-Type: application/json" \
  -d '{"name": "Shopping List"}'

# Response
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Shopping List",
  "createdAt": "2025-09-16T21:30:00Z"
}

# Add an item
curl -X POST http://localhost:8080/api/lists/550e8400-e29b-41d4-a716-446655440000/items \
  -H "Content-Type: application/json" \
  -d '{"text": "Buy groceries"}'

# Response  
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "listId": "550e8400-e29b-41d4-a716-446655440000",
  "text": "Buy groceries",
  "completed": false,
  "createdAt": "2025-09-16T21:31:00Z"
}
```

</details>

---

## 🛠️ Development

### **🔧 Local Development Setup**

```bash
# Format code
./gradlew spotlessApply

# Check code style  
./gradlew checkstyleMain checkstyleTest

# Generate documentation
./gradlew javadoc
open build/docs/javadoc/index.html

# Database console
open http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:file:./data/todoapp
# Username: sa, Password: (empty)
```

### **🐛 Debugging**

```bash
# Debug mode with suspend
./gradlew bootRun --debug-jvm

# Profile performance
./gradlew test --profile
open build/reports/profile/profile-*.html

# Check application health
curl http://localhost:8080/health
```

### **🚀 Production Deployment**

<details>
<summary><strong>🔍 Production Configuration Examples</strong></summary>

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/todoapp
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  security:
    require-ssl: true
  
logging:
  level:
    com.todoapp: INFO
    org.springframework.web: WARN
  file:
    name: /var/log/todoapp/app.log

app:
  database:
    backup-enabled: true
  logging:
    requests:
      include-static-resources: false
```

```dockerfile
# Dockerfile
FROM openjdk:17-jre-slim
COPY build/libs/todo-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

</details>

---

## 🤝 Contributing

### **📋 Development Workflow**

1. **🔀 Fork & Clone**: Fork the repository and clone locally
2. **🌿 Create Branch**: `git checkout -b feature/amazing-feature`
3. **🧪 Write Tests**: Add tests for new functionality (TDD)
4. **💻 Implement**: Write code to make tests pass
5. **✅ Verify**: Run full test suite and code quality checks
6. **📝 Document**: Update relevant documentation
7. **🚀 Submit**: Create pull request with clear description

### **🎯 Code Quality Standards**

- **Test Coverage**: Maintain 100% test coverage for new code
- **Code Style**: Follow existing formatting and naming conventions
- **Documentation**: Update docs for any API or behavior changes
- **Performance**: Ensure changes don't degrade test suite performance

---

## 🆘 Troubleshooting

<details>
<summary><strong>🔍 Common Issues & Solutions</strong></summary>

### **Port Already in Use**
```bash
# Find and kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Or use different port
./gradlew bootRun -Dserver.port=8081
```

### **Database Locked**
```bash
# Stop all Java processes
pkill java

# Remove lock files
rm data/todoapp.*.db
```

### **Tests Failing**
```bash
# Clean everything and retry
./gradlew clean build --rerun-tasks

# Check specific test output
./gradlew test --tests="FailingTest" --info
```

### **Memory Issues**
```bash
# Increase JVM memory
export GRADLE_OPTS="-Xmx2g -Xms1g"
./gradlew bootRun
```

</details>

---

## 📊 Project Stats

- **📁 Lines of Code**: ~5,000 (including tests)
- **🧪 Test Coverage**: 100%  
- **⚡ Test Suite Speed**: <5 seconds
- **🏗️ Architecture Layers**: 5 (Web → Service → Domain → Persistence → Database)
- **🔒 Security Headers**: 7 configured
- **📱 Responsive Breakpoints**: 3 (mobile, tablet, desktop)

---

## 📄 License

This project is available under the [MIT License](LICENSE).

---

## 🎉 Acknowledgments

Built with ❤️ using:
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [H2 Database](https://www.h2database.com/) - Embedded database
- [Thymeleaf](https://www.thymeleaf.org/) - Template engine
- [Gradle](https://gradle.org/) - Build automation
- [JUnit 5](https://junit.org/junit5/) - Testing framework

---

<div align="center">

**⭐ Star this repository if you found it helpful!**

**🐛 Found a bug? [Create an issue](../../issues)**

**💡 Have suggestions? [Start a discussion](../../discussions)**

</div>
