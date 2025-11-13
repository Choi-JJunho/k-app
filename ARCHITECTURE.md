# K-App Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [Architecture Principles](#architecture-principles)
4. [Module Structure](#module-structure)
5. [Layer Architecture](#layer-architecture)
6. [Data Flow](#data-flow)
7. [Security Architecture](#security-architecture)
8. [Database Design](#database-design)
9. [API Design](#api-design)
10. [Development Workflow](#development-workflow)
11. [Build and Deployment](#build-and-deployment)
12. [Testing Strategy](#testing-strategy)

## Overview

**K-App** is a modern Spring Boot REST API application built with Kotlin, following Domain-Driven Design (DDD) and Clean Architecture principles. The application provides user authentication and meal management functionality with a strong emphasis on separation of concerns and maintainability.

### Key Features
- User registration and authentication with JWT
- Secure password hashing with BCrypt
- Meal creation and management
- RESTful API with OpenAPI documentation
- Type-safe SQL with JooQ
- Modular architecture with clear boundaries

## Technology Stack

### Core Technologies
- **Language**: Kotlin 1.9.25
- **Runtime**: Java 21 (LTS)
- **Framework**: Spring Boot 3.5.5
- **Build Tool**: Gradle 8.x (Kotlin DSL)

### Frameworks and Libraries

#### Web Layer
- Spring Web (REST API)
- Spring Security (Authentication/Authorization)
- Springdoc OpenAPI 2.2.0 (API Documentation)

#### Persistence Layer
- JooQ 3.20.7 (Type-safe SQL)
- PostgreSQL Driver
- Spring Security Crypto (BCrypt)

#### Authentication
- JWT (JSON Web Tokens) - io.jsonwebtoken:jjwt 0.11.5

#### Serialization
- Jackson (JSON processing)
- Jackson Kotlin Module

#### Testing
- JUnit 5
- MockK (Kotlin mocking)
- Spring Boot Test
- Testcontainers (Integration testing)

## Architecture Principles

### 1. Clean Architecture
The application follows clean architecture principles with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│            API Layer (Framework)            │
│  Controllers, DTOs, Security, Config        │
└────────────────┬────────────────────────────┘
                 │ depends on
┌────────────────▼────────────────────────────┐
│          Domain Layer (Business)            │
│  Entities, Value Objects, Services          │
│  Repository Interfaces                      │
└────────────────▲────────────────────────────┘
                 │ implemented by
┌────────────────┴────────────────────────────┐
│     Infrastructure Layer (Technical)        │
│  Repository Implementations, JooQ, BCrypt   │
└─────────────────────────────────────────────┘
```

**Key Principles**:
- Domain layer has **no dependencies** on external frameworks
- Outer layers depend on inner layers (Dependency Inversion)
- Infrastructure implements domain interfaces
- Business logic isolated in domain layer

### 2. Domain-Driven Design (DDD)

**Bounded Contexts**:
- **User Context**: User management and authentication
- **Meal Context**: Meal information and management

**Building Blocks**:
- **Aggregate Roots**: User, Meal (entities with identity and lifecycle)
- **Value Objects**: Email, Money, Calories, Menu (immutable, validated)
- **Repository Interfaces**: Abstract data access
- **Domain Services**: Complex operations spanning aggregates
- **Domain Exceptions**: Business rule violations

### 3. Dependency Injection
- Spring IoC container manages all beans
- Constructor injection preferred (immutable, testable)
- Manual bean configuration for domain objects (no Spring in core)

### 4. Fail-Fast Validation
- Validate at boundaries (API layer validation, domain validation)
- Throw exceptions immediately on invalid state
- Value objects validate in constructors

## Module Structure

The application is organized into three distinct Gradle modules:

```
k-app/
├── core/           # Domain logic (pure Kotlin, no frameworks)
│   ├── src/
│   │   └── main/kotlin/koreatech/kapp/domain/
│   │       ├── common/      # Shared domain primitives
│   │       ├── user/        # User bounded context
│   │       └── meal/        # Meal bounded context
│   └── SPEC.md
│
├── infra/          # Infrastructure implementations
│   ├── src/
│   │   └── main/kotlin/koreatech/kapp/
│   │       ├── config/      # Technical configs
│   │       └── persistence/ # JooQ repositories
│   └── SPEC.md
│
├── api/            # REST API layer (Spring Boot)
│   ├── src/
│   │   └── main/kotlin/koreatech/kapp/
│   │       ├── auth/        # Auth infrastructure
│   │       ├── config/      # Spring configs
│   │       ├── global/      # Cross-cutting concerns
│   │       ├── user/        # User feature
│   │       └── meal/        # Meal feature
│   └── SPEC.md
│
├── build.gradle.kts
├── settings.gradle.kts
├── ARCHITECTURE.md
└── CODE_CONVENTIONS.md
```

### Module Dependencies

```
api → infra → core

api:    Spring Boot app, controllers, config
infra:  JooQ, database, password encoding
core:   Pure domain logic, no dependencies
```

**Dependency Rules**:
- API depends on both Core and Infra
- Infra depends on Core (implements interfaces)
- Core depends on nothing (except Kotlin stdlib)
- No circular dependencies allowed

## Layer Architecture

### Layer Responsibilities

#### 1. API Layer (Presentation)
**Location**: `api/` module

**Responsibilities**:
- Expose REST endpoints
- Handle HTTP requests/responses
- Validate input (DTOs with validation annotations)
- Authentication and authorization
- Map between DTOs and domain objects
- Global exception handling
- API documentation (Swagger)

**Technologies**: Spring Boot, Spring Web, Spring Security, JWT

**Key Components**:
- `Controllers`: Handle HTTP requests
- `DTOs`: Data transfer objects for API contracts
- `Services`: Application services (orchestration)
- `Config`: Spring configuration classes
- `GlobalExceptionHandler`: Centralized error handling

**Example Flow**:
```
HTTP Request
    → Controller (validate DTO)
    → Application Service
    → Domain Service (core)
    → Repository Interface (core)
    → Repository Implementation (infra)
```

#### 2. Domain Layer (Business Logic)
**Location**: `core/` module

**Responsibilities**:
- Define business rules and invariants
- Model domain concepts (entities, value objects)
- Define repository interfaces
- Implement domain services
- Throw domain exceptions

**Technologies**: Pure Kotlin (no frameworks)

**Key Components**:
- `Aggregate Roots`: User, Meal
- `Value Objects`: Email, Money, Calories, Menu
- `Repository Interfaces`: Data access abstractions
- `Domain Services`: Complex business operations
- `Domain Exceptions`: Business rule violations

**Characteristics**:
- Framework-independent
- Highly testable
- Expresses ubiquitous language
- Enforces business rules

#### 3. Infrastructure Layer (Technical Details)
**Location**: `infra/` module

**Responsibilities**:
- Implement repository interfaces
- Database access with JooQ
- Password encoding with BCrypt
- Map between records and domain objects
- Handle technical concerns (transactions, caching)

**Technologies**: JooQ, Spring Security Crypto, PostgreSQL

**Key Components**:
- `Repository Implementations`: JooQ-based data access
- `Entity Classes`: Database record mappings
- `Table Definitions`: JooQ table schemas
- `Technical Services`: Password encoding, etc.

**Characteristics**:
- Implements core interfaces
- Hides technical details
- Type-safe SQL with JooQ
- Transaction management

## Data Flow

### Request Flow Diagram

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request (JSON)
       ▼
┌─────────────────────────────────────┐
│         Spring DispatcherServlet     │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│     JWT Authentication Filter        │ (if protected endpoint)
│  - Extract token from header         │
│  - Validate JWT                      │
│  - Load user from database           │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  ArgumentResolver (if @AuthenticatedUser) │
│  - Resolve User from JWT token       │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│         Controller                   │
│  - Validate DTO (@Valid)             │
│  - Call application service          │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│      Application Service             │
│  - Orchestrate use case              │
│  - Convert DTO → Domain              │
│  - Call domain service               │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│       Domain Service (Core)          │
│  - Execute business logic            │
│  - Enforce business rules            │
│  - Call repository                   │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Repository Interface (Core)         │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│  Repository Implementation (Infra)   │
│  - Execute JooQ query                │
│  - Map Record → Entity → Domain      │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────┐
│         PostgreSQL Database          │
└──────┬──────────────────────────────┘
       │
       │ (Response flows back up)
       ▼
┌─────────────┐
│   Client    │ Receives JSON response
└─────────────┘
```

### Example: User Registration Flow

```kotlin
1. POST /api/auth/register
   Body: { "email": "user@example.com", "password": "pass123", "nickname": "John" }

2. AuthController.register(RegisterRequest)
   - Validates DTO (email format, password not blank, etc.)

3. AuthService.register(email, password, nickname)
   - Creates Email value object (validates format)
   - Encodes password with BCrypt
   - Calls UserDomainService

4. UserDomainService.registerUser(email, encodedPassword, nickname)
   - Checks if email already exists (userRepository.existsByEmail)
   - Creates User aggregate root
   - Saves via repository

5. DBUserRepository.save(user)
   - Converts User → UserEntity
   - Executes JooQ INSERT
   - Returns User with generated ID

6. Response flows back:
   UserDomainService → AuthService → AuthController
   - Converts User → UserResponse DTO

7. Returns 201 Created with JSON:
   { "id": 1, "email": "user@example.com", "nickname": "John" }
```

### Example: Login Flow

```kotlin
1. POST /api/auth/login
   Body: { "email": "user@example.com", "password": "pass123" }

2. AuthController.login(LoginRequest)

3. AuthService.login(email, password)
   - Finds user by email (userRepository.findByEmail)
   - Verifies password (passwordEncoder.matches)
   - Generates JWT token (jwtService.generateToken)

4. Returns 200 OK with JSON:
   {
     "token": "eyJhbGciOiJIUzI1NiIs...",
     "user": { "id": 1, "email": "user@example.com", "nickname": "John" }
   }
```

### Example: Protected Endpoint Flow

```kotlin
1. GET /api/auth/me
   Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

2. JWT Authentication Filter
   - Extracts token from header
   - Validates JWT signature and expiration
   - Extracts user ID from token

3. AuthenticatedUserArgumentResolver
   - Loads User from database by ID
   - Injects User into controller method

4. AuthController.getCurrentUser(@AuthenticatedUser user)
   - Converts User → UserResponse DTO

5. Returns 200 OK with user data
```

## Security Architecture

### Authentication Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /api/auth/login
       │    { email, password }
       ▼
┌─────────────────────────┐
│    AuthController        │
└──────┬──────────────────┘
       │
       │ 2. AuthService.login()
       ▼
┌─────────────────────────┐
│    AuthService           │
│  - Find user by email    │
│  - Verify password       │
│  - Generate JWT token    │
└──────┬──────────────────┘
       │
       │ 3. Returns LoginResponse
       │    { token, user }
       ▼
┌─────────────┐
│   Client    │ Stores token
└─────────────┘

┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 4. GET /api/protected-resource
       │    Header: Authorization: Bearer <token>
       ▼
┌─────────────────────────┐
│  JWT Auth Filter         │
│  - Extract token         │
│  - Validate signature    │
│  - Check expiration      │
│  - Extract user ID       │
└──────┬──────────────────┘
       │
       │ 5. Load user from DB
       ▼
┌─────────────────────────┐
│  ArgumentResolver        │
│  - Query user by ID      │
│  - Inject into method    │
└──────┬──────────────────┘
       │
       │ 6. Execute business logic
       ▼
┌─────────────────────────┐
│    Controller            │
└──────┬──────────────────┘
       │
       │ 7. Returns protected data
       ▼
┌─────────────┐
│   Client    │
└─────────────┘
```

### Security Mechanisms

#### 1. Password Security
- **Hashing Algorithm**: BCrypt with salt
- **Storage**: Only hashed passwords stored in database
- **Verification**: One-way hash comparison
- **Strength**: BCrypt strength 10+ (configurable)

#### 2. JWT Tokens
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Claims**:
  - `sub`: User ID
  - `email`: User email
  - `nickname`: User nickname
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp
  - `iss`: Token issuer
- **Expiration**: 24 hours (configurable)
- **Secret**: 256-bit secret key (externalized in config)

#### 3. Endpoint Protection
- **Public Endpoints**:
  - POST /api/auth/register
  - POST /api/auth/login
  - GET /swagger-ui/**
  - GET /v3/api-docs/**

- **Protected Endpoints**:
  - GET /api/auth/me
  - All /api/meals/** endpoints
  - Require valid JWT in Authorization header

#### 4. Input Validation
- **DTO Validation**: Bean Validation annotations (@Email, @NotBlank, @Size, etc.)
- **Domain Validation**: Value objects validate in constructors
- **SQL Injection Prevention**: JooQ parameterized queries

#### 5. Error Handling Security
- Never expose sensitive information in error messages
- Generic error messages for authentication failures
- Detailed errors only in development mode
- No stack traces in production

## Database Design

### Schema Overview

```sql
-- Users Table
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,        -- BCrypt hashed
    nickname    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Meals Table
CREATE TABLE meals (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    price       INTEGER NOT NULL CHECK (price >= 0),
    calories    INTEGER NOT NULL CHECK (calories >= 0),
    menu        JSONB NOT NULL,               -- Array of menu items
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meals_name ON meals(name);
```

### Design Decisions

1. **BIGSERIAL for IDs**: Supports large-scale growth
2. **Email UNIQUE constraint**: Enforces uniqueness at database level
3. **CHECK constraints**: Database-level validation for non-negative values
4. **JSONB for menu**: Flexible schema for menu items, supports indexing
5. **Timestamps**: Audit trail for creation and modification
6. **Indexes**: Optimize common queries (email lookup, meal search)

### Data Types Mapping

| Domain Type | Database Type | Notes |
|------------|---------------|-------|
| Long? | BIGSERIAL | Auto-incrementing ID |
| Email | VARCHAR(255) | Validated email string |
| String | VARCHAR(255) | Text fields |
| Money | INTEGER | Store in smallest currency unit |
| Calories | INTEGER | Calorie count |
| Menu | JSONB | Flexible array storage |

## API Design

### REST API Principles

1. **Resource-Based URLs**: Use nouns, not verbs
2. **HTTP Methods**: Standard semantics (GET, POST, PUT, PATCH, DELETE)
3. **Status Codes**: Appropriate HTTP status codes
4. **JSON**: Request and response format
5. **Versioning**: Consider /api/v1 prefix for future versioning

### Endpoints Overview

#### Authentication Endpoints

| Method | Endpoint | Description | Auth Required | Request Body | Response |
|--------|----------|-------------|---------------|--------------|----------|
| POST | /api/auth/register | Register new user | No | RegisterRequest | 201 Created, UserResponse |
| POST | /api/auth/login | Login user | No | LoginRequest | 200 OK, LoginResponse |
| GET | /api/auth/me | Get current user | Yes | - | 200 OK, UserResponse |

#### Meal Endpoints

| Method | Endpoint | Description | Auth Required | Request Body | Response |
|--------|----------|-------------|---------------|--------------|----------|
| POST | /api/meals | Create meal | Yes | CreateMealRequest | 201 Created, MealResponse |
| GET | /api/meals | List all meals | Yes/No | - | 200 OK, List<MealResponse> |
| GET | /api/meals/{id} | Get meal by ID | Yes/No | - | 200 OK, MealResponse |

### Response Format

**Success Response**:
```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "John Doe"
}
```

**Error Response**:
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/api/auth/register"
}
```

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- Interactive documentation with try-out functionality

## Development Workflow

### Local Development Setup

1. **Prerequisites**:
   ```bash
   - Java 21
   - Kotlin 1.9.25
   - PostgreSQL 15+
   - Gradle 8.x (or use wrapper)
   ```

2. **Database Setup**:
   ```bash
   # Create database
   createdb kapp

   # Run migrations (if using Flyway/Liquibase)
   ./gradlew flywayMigrate
   ```

3. **Configuration**:
   ```yaml
   # api/src/main/resources/application-local.yml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/kapp
       username: postgres
       password: your-password
   ```

4. **Build and Run**:
   ```bash
   # Build all modules
   ./gradlew build

   # Run application
   ./gradlew :api:bootRun

   # Or run main class
   ./gradlew :api:bootJar
   java -jar api/build/libs/api-0.0.1-SNAPSHOT.jar
   ```

5. **Access Application**:
   - API: http://localhost:8080
   - Swagger: http://localhost:8080/swagger-ui.html

### Development Guidelines

1. **Feature Development**:
   - Start with domain model (core module)
   - Implement repository interface
   - Add repository implementation (infra module)
   - Create DTOs and controller (api module)
   - Write tests at each layer

2. **Branch Strategy**:
   - `main`: Production-ready code
   - `develop`: Integration branch
   - `feature/*`: Feature branches
   - `fix/*`: Bug fix branches

3. **Code Review**:
   - All changes via pull requests
   - At least one approval required
   - CI checks must pass
   - Follow code conventions

## Build and Deployment

### Build Configuration

```kotlin
// settings.gradle.kts
rootProject.name = "k-app"
include("core", "infra", "api")

// build.gradle.kts (root)
plugins {
    kotlin("jvm") version "1.9.25"
    id("org.springframework.boot") version "3.5.5" apply false
}

allprojects {
    group = "koreatech.kapp"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}
```

### Gradle Tasks

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run specific module tests
./gradlew :core:test

# Build bootable JAR
./gradlew :api:bootJar

# Run application
./gradlew :api:bootRun

# Check dependencies
./gradlew dependencies

# Generate JooQ code
./gradlew generateJooq
```

### Docker Deployment

**Dockerfile**:
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY api/build/libs/api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: kapp
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/kapp
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - postgres

volumes:
  postgres-data:
```

**Commands**:
```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f api

# Stop services
docker-compose down
```

## Testing Strategy

### Testing Pyramid

```
        ┌─────────────┐
        │    E2E      │  Few comprehensive end-to-end tests
        └─────────────┘
       ┌───────────────┐
       │  Integration  │  Moderate integration tests
       └───────────────┘
      ┌─────────────────┐
      │   Unit Tests    │  Many fast unit tests
      └─────────────────┘
```

### Test Types

#### 1. Unit Tests (Core Module)
- Test domain logic in isolation
- Mock repository interfaces
- Fast execution (milliseconds)
- No external dependencies

**Example**:
```kotlin
class UserDomainServiceTest {
    @Test
    fun `should register user with valid email`() {
        val repo = mockk<UserRepository>()
        every { repo.existsByEmail(any()) } returns false
        every { repo.save(any()) } answers { firstArg() }

        val service = UserDomainService(repo)
        val user = service.registerUser(Email("test@example.com"), "hashed", "Test")

        assertThat(user.email.value).isEqualTo("test@example.com")
    }
}
```

#### 2. Integration Tests (Infra Module)
- Test repository implementations
- Use real database (Testcontainers)
- Verify SQL queries and mappings

**Example**:
```kotlin
@SpringBootTest
@Testcontainers
class DBUserRepositoryTest {
    @Container
    val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")

    @Test
    fun `should save and find user`() {
        val user = User(null, Email("test@example.com"), "hashed", "Test")
        val saved = repository.save(user)

        val found = repository.findByEmail(Email("test@example.com"))

        assertThat(found).isNotNull()
        assertThat(found?.id).isEqualTo(saved.id)
    }
}
```

#### 3. API Tests (API Module)
- Test controllers with MockMvc
- Verify HTTP request/response handling
- Mock services to isolate controller logic

**Example**:
```kotlin
@WebMvcTest(AuthController::class)
class AuthControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var authService: AuthService

    @Test
    fun `should register user`() {
        val request = RegisterRequest("test@example.com", "password", "Test")
        val user = User(1L, Email("test@example.com"), "hashed", "Test")
        every { authService.register(any(), any(), any()) } returns user

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("test@example.com"))
    }
}
```

#### 4. End-to-End Tests
- Test complete user journeys
- Use TestRestTemplate or REST Assured
- Run against full application context

**Example**:
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserJourneyE2ETest {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `complete user registration and login flow`() {
        // Register
        val registerRequest = RegisterRequest("e2e@example.com", "password123", "E2E User")
        val registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            registerRequest,
            UserResponse::class.java
        )
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        // Login
        val loginRequest = LoginRequest("e2e@example.com", "password123")
        val loginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            LoginResponse::class.java
        )
        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        val token = loginResponse.body?.token
        assertThat(token).isNotNull()

        // Access protected endpoint
        val headers = HttpHeaders()
        headers.setBearerAuth(token!!)
        val entity = HttpEntity<Void>(headers)
        val meResponse = restTemplate.exchange(
            "/api/auth/me",
            HttpMethod.GET,
            entity,
            UserResponse::class.java
        )
        assertThat(meResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(meResponse.body?.email).isEqualTo("e2e@example.com")
    }
}
```

### Test Coverage Goals
- **Unit Tests**: 80%+ coverage
- **Integration Tests**: All repository methods
- **API Tests**: All endpoints and error cases
- **E2E Tests**: Critical user journeys

### Continuous Integration
```yaml
# .github/workflows/ci.yml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build with Gradle
        run: ./gradlew build

      - name: Run tests
        run: ./gradlew test

      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Monitoring and Observability

### Health Checks
```kotlin
@RestController
@RequestMapping("/actuator")
class HealthController {
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        return mapOf(
            "status" to "UP",
            "timestamp" to Instant.now()
        )
    }
}
```

### Logging Strategy
```kotlin
// Use SLF4J with Logback
class AuthService(/* deps */) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun login(email: String, password: String): LoginResponse {
        logger.info("Login attempt for email: {}", email)
        // ...
        logger.info("Login successful for email: {}", email)
    }
}
```

### Metrics (Future Enhancement)
- Spring Boot Actuator metrics
- Prometheus for metric collection
- Grafana for visualization
- Track: request rates, error rates, response times

## Future Enhancements

### Planned Features
1. **User Management**: Update profile, change password, delete account
2. **Meal Management**: Update, delete, search, filter meals
3. **Role-Based Access**: Admin, user roles with permissions
4. **Pagination**: Support for large datasets
5. **File Upload**: Profile pictures, meal images
6. **Caching**: Redis for frequently accessed data
7. **Rate Limiting**: Prevent API abuse
8. **Audit Logging**: Track all changes
9. **Notifications**: Email, push notifications
10. **API Versioning**: Support multiple API versions

### Technical Improvements
1. **Database Migrations**: Flyway or Liquibase
2. **Distributed Tracing**: Zipkin or Jaeger
3. **API Gateway**: Spring Cloud Gateway
4. **Service Mesh**: Istio for microservices
5. **Event-Driven Architecture**: Kafka or RabbitMQ
6. **CQRS**: Separate read/write models
7. **GraphQL**: Alternative to REST
8. **WebSocket**: Real-time updates

## References

### Documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [JooQ Documentation](https://www.jooq.org/doc/latest/manual/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [JWT Documentation](https://jwt.io/introduction)

### Design Patterns
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design by Eric Evans](https://domainlanguage.com/ddd/)
- [RESTful API Design](https://restfulapi.net/)

### Module Specifications
- [Core Module Spec](./core/SPEC.md)
- [Infra Module Spec](./infra/SPEC.md)
- [API Module Spec](./api/SPEC.md)
- [Code Conventions](./CODE_CONVENTIONS.md)

---

**Last Updated**: 2025-11-13
**Version**: 1.0.0
**Maintainers**: K-App Development Team
