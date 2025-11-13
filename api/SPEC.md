# API Module Specification

## Overview
The **API Module** is the presentation layer that exposes REST APIs using Spring Boot. It handles HTTP requests, authentication, authorization, request/response mapping, and error handling. This module orchestrates calls to domain services and translates domain concepts to HTTP concepts.

## Technology Stack
- Kotlin 1.9.25
- Java 21
- Spring Boot 3.5.5
- Spring Web (REST API)
- Spring Security (JWT Authentication)
- Springdoc OpenAPI (Swagger documentation)
- Jackson (JSON serialization)

## Architecture Principles
- **Thin Controller Layer**: Controllers should be thin, delegating to services
- **DTO Pattern**: Use DTOs for request/response, never expose domain objects directly
- **Declarative Security**: Use annotations for security configuration
- **Centralized Error Handling**: Global exception handler for consistent error responses
- **API Documentation**: Auto-generated Swagger/OpenAPI specs

## Directory Structure

```
api/
└── src/main/kotlin/koreatech/kapp/
    ├── KAppApplication.kt                     # Spring Boot main class
    ├── auth/                                  # Authentication infrastructure
    │   ├── AuthenticatedUser.kt               # Annotation for user injection
    │   └── AuthenticatedUserArgumentResolver.kt # Resolver for authenticated user
    ├── config/                                # Application configuration
    │   ├── ApplicationConfig.kt               # Web MVC and security config
    │   └── SwaggerConfig.kt                   # API documentation config
    ├── global/                                # Global cross-cutting concerns
    │   ├── ErrorResponse.kt                   # Standard error response DTO
    │   └── GlobalExceptionHandler.kt          # Centralized exception handling
    ├── user/                                  # User feature module
    │   ├── controller/
    │   │   ├── AuthController.kt              # Auth endpoints
    │   │   └── dto/
    │   │       └── UserDtos.kt                # User request/response DTOs
    │   ├── service/
    │   │   └── AuthService.kt                 # User application service
    │   ├── jwt/
    │   │   ├── JwtService.kt                  # JWT token management
    │   │   └── JwtProperties.kt               # JWT configuration properties
    │   └── UserBeanConfig.kt                  # User module bean definitions
    └── meal/                                  # Meal feature module
        ├── controller/
        │   ├── MealController.kt              # Meal endpoints
        │   └── dto/
        │       └── MealDtos.kt                # Meal request/response DTOs
        ├── service/
        │   └── MealService.kt                 # Meal application service
        └── MealBeanConfig.kt                  # Meal module bean definitions
```

## Component Specifications

### 1. Application Entry Point

#### KAppApplication
**Purpose**: Spring Boot application entry point

**Specifications**:
- Main class with `@SpringBootApplication`
- Component scanning for all packages
- Auto-configuration enabled
- Runs embedded Tomcat server

**Example**:
```kotlin
@SpringBootApplication
class KAppApplication

fun main(args: Array<String>) {
    runApplication<KAppApplication>(*args)
}
```

### 2. Configuration

#### ApplicationConfig
**Purpose**: Configure Web MVC, security, and application-wide beans

**Specifications**:
- `@Configuration` class
- Register custom argument resolvers
- Configure CORS if needed
- Setup security filter chain
- Bean definitions for core and infra dependencies

**Key Configurations**:
1. **Argument Resolvers**: Register `AuthenticatedUserArgumentResolver`
2. **Security**: JWT authentication filter chain
3. **CORS**: Configure allowed origins, methods, headers
4. **Bean Wiring**: Inject dependencies from core and infra modules

**Example**:
```kotlin
@Configuration
class ApplicationConfig {

    @Bean
    fun webMvcConfigurer(
        authenticatedUserResolver: AuthenticatedUserArgumentResolver
    ): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addArgumentResolvers(
                resolvers: MutableList<HandlerMethodArgumentResolver>
            ) {
                resolvers.add(authenticatedUserResolver)
            }
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
```

#### SwaggerConfig
**Purpose**: Configure Swagger/OpenAPI documentation

**Specifications**:
- `@Configuration` class
- Define API metadata (title, version, description)
- Configure JWT authentication in Swagger UI
- Set up security schemes

**Example**:
```kotlin
@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("K-App API")
                    .version("1.0")
                    .description("K-App REST API Documentation")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "Bearer",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("Bearer"))
    }
}
```

**Access Swagger UI**: `http://localhost:8080/swagger-ui.html`

### 3. Global Error Handling

#### ErrorResponse
**Purpose**: Standard error response structure

**Specifications**:
- Data class for consistent error format
- **Fields**:
  - `timestamp: String` (ISO-8601 format)
  - `status: Int` (HTTP status code)
  - `error: String` (error type)
  - `message: String` (error description)
  - `path: String?` (request path)

**Example**:
```kotlin
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String?
)
```

**Example Response**:
```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/api/auth/register"
}
```

#### GlobalExceptionHandler
**Purpose**: Centralized exception handling for all controllers

**Specifications**:
- `@RestControllerAdvice` annotation
- Handle all exception types
- Map domain exceptions to HTTP status codes
- Return consistent ErrorResponse format

**Exception Mappings**:
1. **DomainException** → 400 Bad Request (business rule violations)
2. **IllegalArgumentException** → 400 Bad Request (validation errors)
3. **NoSuchElementException** → 404 Not Found (resource not found)
4. **AccessDeniedException** → 403 Forbidden (authorization failure)
5. **AuthenticationException** → 401 Unauthorized (authentication failure)
6. **Exception** → 500 Internal Server Error (unexpected errors)

**Example**:
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Domain Error",
            message = ex.message ?: "Business rule violation",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        val error = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
```

### 4. Authentication Infrastructure

#### JwtProperties
**Purpose**: Externalize JWT configuration

**Specifications**:
- `@ConfigurationProperties("jwt")` annotation
- Load from `application.yml`
- **Properties**:
  - `secret: String` (JWT signing key)
  - `expirationMs: Long` (token expiration time)
  - `issuer: String?` (token issuer)

**Example Configuration** (`application.yml`):
```yaml
jwt:
  secret: your-256-bit-secret-key-change-in-production
  expiration-ms: 86400000  # 24 hours
  issuer: k-app
```

**Example Class**:
```kotlin
@ConfigurationProperties("jwt")
data class JwtProperties(
    val secret: String,
    val expirationMs: Long,
    val issuer: String = "k-app"
)
```

#### JwtService
**Purpose**: Create and validate JWT tokens

**Specifications**:
- Component for JWT operations
- Use library like `jjwt` (Java JWT)
- Thread-safe implementation

**Methods**:
1. **generateToken(user: User): String**
   - Create JWT with user claims
   - Set expiration time
   - Sign with secret key
   - Include user ID and email in claims

2. **validateToken(token: String): Long?**
   - Parse and validate JWT
   - Check signature
   - Check expiration
   - Return user ID if valid, null otherwise

3. **extractToken(request: HttpServletRequest): String?**
   - Extract JWT from Authorization header
   - Handle "Bearer " prefix
   - Return null if not present

**Example Implementation**:
```kotlin
@Component
class JwtService(
    private val jwtProperties: JwtProperties
) {
    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.expirationMs)

        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("email", user.email.value)
            .claim("nickname", user.nickname)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .setIssuer(jwtProperties.issuer)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun validateToken(token: String): Long? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims.subject.toLong()
        } catch (ex: Exception) {
            null  // Invalid token
        }
    }

    fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
```

#### AuthenticatedUser Annotation
**Purpose**: Mark controller parameters for authenticated user injection

**Specifications**:
- Custom parameter annotation
- Used with `AuthenticatedUserArgumentResolver`
- Enables clean controller method signatures

**Example**:
```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthenticatedUser
```

**Usage in Controller**:
```kotlin
@GetMapping("/me")
fun getCurrentUser(@AuthenticatedUser user: User): UserResponse {
    return UserResponse.from(user)
}
```

#### AuthenticatedUserArgumentResolver
**Purpose**: Resolve authenticated user from JWT token

**Specifications**:
- Implements `HandlerMethodArgumentResolver`
- Extract JWT from request
- Validate token and load user
- Inject user into controller method

**Example Implementation**:
```kotlin
@Component
class AuthenticatedUserArgumentResolver(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthenticatedUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("Cannot get HttpServletRequest")

        val token = jwtService.extractToken(request)
            ?: throw AuthenticationException("Missing authentication token")

        val userId = jwtService.validateToken(token)
            ?: throw AuthenticationException("Invalid or expired token")

        return userRepository.findById(userId)
            ?: throw AuthenticationException("User not found")
    }
}
```

### 5. User Feature Module

#### UserDtos
**Purpose**: Request and response DTOs for user endpoints

**Specifications**:
- Place all user DTOs in single file
- Use data classes
- Validation annotations from `javax.validation`

**DTOs**:

1. **RegisterRequest**:
```kotlin
data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:NotBlank(message = "Nickname is required")
    val nickname: String
)
```

2. **LoginRequest**:
```kotlin
data class LoginRequest(
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)
```

3. **LoginResponse**:
```kotlin
data class LoginResponse(
    val token: String,
    val user: UserResponse
)
```

4. **UserResponse**:
```kotlin
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String
) {
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id ?: throw IllegalStateException("User ID is null"),
                email = user.email.value,
                nickname = user.nickname
            )
        }
    }
}
```

#### AuthController
**Purpose**: Handle authentication endpoints

**Specifications**:
- `@RestController` with base path `/api/auth`
- Delegate to `AuthService`
- Use DTOs for requests/responses
- Apply validation with `@Valid`

**Endpoints**:

1. **POST /api/auth/register** - Register new user
   - Request: `RegisterRequest`
   - Response: `UserResponse`
   - Status: 201 Created

2. **POST /api/auth/login** - Login user
   - Request: `LoginRequest`
   - Response: `LoginResponse` (includes JWT token)
   - Status: 200 OK

3. **GET /api/auth/me** - Get current user
   - Requires authentication
   - Response: `UserResponse`
   - Status: 200 OK

**Example Implementation**:
```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): UserResponse {
        val user = authService.register(
            email = request.email,
            password = request.password,
            nickname = request.nickname
        )
        return UserResponse.from(user)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        return authService.login(
            email = request.email,
            password = request.password
        )
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticatedUser user: User): UserResponse {
        return UserResponse.from(user)
    }
}
```

#### AuthService
**Purpose**: Application service for authentication operations

**Specifications**:
- `@Service` annotation
- Orchestrate domain services and repositories
- Handle password encoding
- Generate JWT tokens

**Methods**:

1. **register(email: String, password: String, nickname: String): User**
   - Validate email format
   - Check email uniqueness
   - Encode password
   - Create user via domain service
   - Return created user

2. **login(email: String, password: String): LoginResponse**
   - Find user by email
   - Verify password
   - Generate JWT token
   - Return token and user data

**Example Implementation**:
```kotlin
@Service
class AuthService(
    private val userDomainService: UserDomainService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun register(email: String, password: String, nickname: String): User {
        val emailVO = Email(email)
        val encodedPassword = passwordEncoder.encode(password)
        return userDomainService.registerUser(emailVO, encodedPassword, nickname)
    }

    fun login(email: String, password: String): LoginResponse {
        val emailVO = Email(email)
        val user = userRepository.findByEmail(emailVO)
            ?: throw DomainException("Invalid email or password")

        if (!passwordEncoder.matches(password, user.password)) {
            throw DomainException("Invalid email or password")
        }

        val token = jwtService.generateToken(user)
        return LoginResponse(
            token = token,
            user = UserResponse.from(user)
        )
    }
}
```

#### UserBeanConfig
**Purpose**: Define user module Spring beans

**Specifications**:
- `@Configuration` class
- Create beans for domain services and repositories
- Wire dependencies from core and infra modules

**Example**:
```kotlin
@Configuration
class UserBeanConfig {

    @Bean
    fun userDomainService(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): UserDomainService {
        return UserDomainService(userRepository, passwordEncoder)
    }
}
```

### 6. Meal Feature Module

#### MealDtos
**Purpose**: Request and response DTOs for meal endpoints

**DTOs**:

1. **CreateMealRequest**:
```kotlin
data class CreateMealRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:Min(value = 0, message = "Price must be non-negative")
    val price: Int,

    @field:Min(value = 0, message = "Calories must be non-negative")
    val calories: Int,

    val menu: List<String>
)
```

2. **MealResponse**:
```kotlin
data class MealResponse(
    val id: Long,
    val name: String,
    val price: Int,
    val calories: Int,
    val menu: List<String>
) {
    companion object {
        fun from(meal: Meal): MealResponse {
            return MealResponse(
                id = meal.id ?: throw IllegalStateException("Meal ID is null"),
                name = meal.name,
                price = meal.price.amount,
                calories = meal.calories.value,
                menu = meal.menu.items
            )
        }
    }
}
```

#### MealController
**Purpose**: Handle meal-related endpoints

**Specifications**:
- `@RestController` with base path `/api/meals`
- Require authentication for all endpoints
- Use DTOs for requests/responses

**Endpoints**:

1. **POST /api/meals** - Create new meal
   - Request: `CreateMealRequest`
   - Response: `MealResponse`
   - Status: 201 Created
   - Requires authentication

2. **GET /api/meals** - List all meals
   - Response: `List<MealResponse>`
   - Status: 200 OK
   - Public or authenticated (decide based on requirements)

3. **GET /api/meals/{id}** - Get meal by ID
   - Response: `MealResponse`
   - Status: 200 OK, 404 Not Found
   - Public or authenticated

**Example Implementation**:
```kotlin
@RestController
@RequestMapping("/api/meals")
class MealController(
    private val mealService: MealService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMeal(
        @Valid @RequestBody request: CreateMealRequest,
        @AuthenticatedUser user: User
    ): MealResponse {
        val meal = mealService.createMeal(
            name = request.name,
            price = request.price,
            calories = request.calories,
            menu = request.menu
        )
        return MealResponse.from(meal)
    }

    @GetMapping
    fun listMeals(): List<MealResponse> {
        return mealService.getAllMeals()
            .map { MealResponse.from(it) }
    }

    @GetMapping("/{id}")
    fun getMeal(@PathVariable id: Long): MealResponse {
        val meal = mealService.getMealById(id)
            ?: throw NoSuchElementException("Meal not found with id: $id")
        return MealResponse.from(meal)
    }
}
```

#### MealService
**Purpose**: Application service for meal operations

**Specifications**:
- `@Service` annotation
- Orchestrate meal domain operations
- Handle business logic at application level

**Methods**:

1. **createMeal(name: String, price: Int, calories: Int, menu: List<String>): Meal**
   - Create value objects
   - Call domain service
   - Return created meal

2. **getAllMeals(): List<Meal>**
   - Fetch from repository
   - Return all meals

3. **getMealById(id: Long): Meal?**
   - Find by ID
   - Return null if not found

**Example Implementation**:
```kotlin
@Service
class MealService(
    private val mealDomainService: MealDomainService,
    private val mealRepository: MealRepository
) {

    fun createMeal(name: String, price: Int, calories: Int, menu: List<String>): Meal {
        return mealDomainService.createMeal(
            name = name,
            price = Money(price),
            calories = Calories(calories),
            menu = Menu(menu)
        )
    }

    fun getAllMeals(): List<Meal> {
        return mealRepository.findAll()
    }

    fun getMealById(id: Long): Meal? {
        return mealRepository.findById(id)
    }
}
```

#### MealBeanConfig
**Purpose**: Define meal module Spring beans

**Example**:
```kotlin
@Configuration
class MealBeanConfig {

    @Bean
    fun mealDomainService(mealRepository: MealRepository): MealDomainService {
        return MealDomainService(mealRepository)
    }
}
```

## API Documentation

### Swagger/OpenAPI
**Access**: http://localhost:8080/swagger-ui.html

**Features**:
- Interactive API documentation
- Try-out functionality
- Request/response schemas
- Authentication support (Bearer token)

### Adding API Documentation

**Controller Level**:
```kotlin
@Tag(name = "Authentication", description = "User authentication endpoints")
@RestController
@RequestMapping("/api/auth")
class AuthController { ... }
```

**Endpoint Level**:
```kotlin
@Operation(
    summary = "Register new user",
    description = "Creates a new user account with email, password, and nickname"
)
@ApiResponses(
    value = [
        ApiResponse(responseCode = "201", description = "User created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    ]
)
@PostMapping("/register")
fun register(@RequestBody request: RegisterRequest): UserResponse { ... }
```

## Development Guidelines

### Adding New Endpoints

1. **Define DTOs**:
   - Create request/response DTOs in `dto` package
   - Add validation annotations
   - Include mapping methods (from/to domain)

2. **Create Controller**:
   - Annotate with `@RestController`
   - Define base path with `@RequestMapping`
   - Keep methods thin, delegate to service
   - Use appropriate HTTP methods and status codes

3. **Implement Service**:
   - Annotate with `@Service`
   - Orchestrate domain operations
   - Handle application-level concerns
   - Return domain objects

4. **Add API Documentation**:
   - Use Swagger annotations
   - Document request/response schemas
   - Specify status codes
   - Add descriptions

5. **Write Tests**:
   - Integration tests with `@SpringBootTest`
   - Mock MVC tests for controllers
   - Test authentication and authorization
   - Verify error handling

### REST API Best Practices

1. **HTTP Methods**:
   - GET: Retrieve resources (idempotent)
   - POST: Create resources
   - PUT: Update entire resource (idempotent)
   - PATCH: Partial update
   - DELETE: Remove resource (idempotent)

2. **Status Codes**:
   - 200 OK: Success (GET, PUT, PATCH)
   - 201 Created: Resource created (POST)
   - 204 No Content: Success with no body (DELETE)
   - 400 Bad Request: Validation error
   - 401 Unauthorized: Authentication required
   - 403 Forbidden: Insufficient permissions
   - 404 Not Found: Resource not found
   - 500 Internal Server Error: Unexpected error

3. **URL Design**:
   - Use nouns, not verbs: `/api/users`, not `/api/getUsers`
   - Use plural nouns: `/api/meals`, not `/api/meal`
   - Nest resources logically: `/api/users/{id}/orders`
   - Use query params for filtering: `/api/meals?category=lunch`

4. **Versioning**:
   - Consider URL versioning: `/api/v1/users`
   - Or header versioning: `Accept: application/vnd.kapp.v1+json`
   - Document breaking changes

### Security Guidelines

1. **Authentication**:
   - Use JWT for stateless authentication
   - Include token in Authorization header: `Bearer <token>`
   - Validate token on every protected request
   - Handle token expiration gracefully

2. **Authorization**:
   - Check user permissions before operations
   - Use method security: `@PreAuthorize("hasRole('ADMIN')")`
   - Implement resource-level access control

3. **Input Validation**:
   - Use `@Valid` for automatic validation
   - Define custom validators for complex rules
   - Sanitize input to prevent injection attacks
   - Validate file uploads

4. **Sensitive Data**:
   - Never log passwords or tokens
   - Exclude password from JSON responses
   - Use HTTPS in production
   - Implement rate limiting

### Testing Guidelines

**Controller Tests**:
```kotlin
@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var authService: AuthService

    @Test
    fun `should register user successfully`() {
        // Given
        val request = RegisterRequest("test@example.com", "password123", "Test User")
        val user = User(1L, Email("test@example.com"), "hashed", "Test User")
        every { authService.register(any(), any(), any()) } returns user

        // When & Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.nickname").value("Test User"))
    }
}
```

**Integration Tests**:
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should register and login user`() {
        // Register
        val registerRequest = RegisterRequest("test@example.com", "password123", "Test")
        val registerResponse = restTemplate.postForEntity(
            "/api/auth/register",
            registerRequest,
            UserResponse::class.java
        )
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        // Login
        val loginRequest = LoginRequest("test@example.com", "password123")
        val loginResponse = restTemplate.postForEntity(
            "/api/auth/login",
            loginRequest,
            LoginResponse::class.java
        )
        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(loginResponse.body?.token).isNotNull()
    }
}
```

## Configuration Files

### application.yml
```yaml
spring:
  application:
    name: k-app

  datasource:
    url: jdbc:postgresql://localhost:5432/kapp
    username: postgres
    password: password
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080
  error:
    include-message: always
    include-stacktrace: never

jwt:
  secret: your-256-bit-secret-key-change-in-production
  expiration-ms: 86400000  # 24 hours
  issuer: k-app

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

logging:
  level:
    koreatech.kapp: DEBUG
    org.springframework.web: INFO
```

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

jwt:
  secret: test-secret-key-for-testing-only
  expiration-ms: 3600000
```

## Dependencies

### build.gradle.kts
```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":infra"))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
```

## Deployment Considerations

### Production Checklist
- [ ] Change JWT secret to strong random key
- [ ] Enable HTTPS only
- [ ] Configure CORS properly
- [ ] Set up rate limiting
- [ ] Enable security headers
- [ ] Configure logging levels
- [ ] Set up health checks
- [ ] Configure connection pooling
- [ ] Enable production profiles
- [ ] Set appropriate timeouts

### Health Checks
```kotlin
@RestController
@RequestMapping("/actuator")
class HealthController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP")
    }
}
```

## Future Enhancements

1. **API Versioning**: Support multiple API versions
2. **Rate Limiting**: Prevent abuse with request throttling
3. **Pagination**: Add pagination support for list endpoints
4. **Filtering**: Advanced filtering and search capabilities
5. **File Upload**: Support file uploads for avatars, images
6. **WebSocket**: Real-time communication support
7. **Caching**: Response caching with Redis
8. **Compression**: Enable gzip compression
9. **Internationalization**: Multi-language support
10. **Audit Logging**: Track all API requests

## Review Checklist

Before committing changes to API module:
- [ ] Controllers are thin, delegating to services
- [ ] DTOs used for all requests/responses
- [ ] Validation annotations on DTOs
- [ ] Proper HTTP methods and status codes
- [ ] Authentication/authorization implemented
- [ ] Error handling via GlobalExceptionHandler
- [ ] Swagger documentation added
- [ ] Integration tests cover endpoints
- [ ] Security best practices followed
- [ ] Configuration externalized
