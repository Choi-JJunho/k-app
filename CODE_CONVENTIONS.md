# K-App Code Conventions

## Table of Contents
1. [General Principles](#general-principles)
2. [Kotlin Style Guide](#kotlin-style-guide)
3. [Package Structure](#package-structure)
4. [Naming Conventions](#naming-conventions)
5. [Code Organization](#code-organization)
6. [Domain-Driven Design Conventions](#domain-driven-design-conventions)
7. [Spring Framework Conventions](#spring-framework-conventions)
8. [Database Conventions](#database-conventions)
9. [API Conventions](#api-conventions)
10. [Testing Conventions](#testing-conventions)
11. [Documentation Standards](#documentation-standards)
12. [Git Conventions](#git-conventions)

## General Principles

### Core Values
1. **Readability First**: Code is read more often than written
2. **Consistency**: Follow established patterns throughout the codebase
3. **Simplicity**: Prefer simple solutions over clever ones
4. **Explicitness**: Make intentions clear, avoid implicit behavior
5. **Testability**: Write code that's easy to test

### Code Quality Standards
- **No compiler warnings**: Resolve all warnings before committing
- **No magic numbers**: Use named constants
- **DRY (Don't Repeat Yourself)**: Extract common logic
- **SOLID principles**: Follow object-oriented design principles
- **Single Responsibility**: Each class/function should have one clear purpose

## Kotlin Style Guide

### File Organization

```kotlin
// 1. Package declaration
package koreatech.kapp.domain.user.model

// 2. Imports (organized automatically)
import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.common.DomainException

// 3. Top-level constants (if any)
private const val MAX_NICKNAME_LENGTH = 50

// 4. Class/interface declaration
class User(
    val id: Long?,
    val email: Email,
    val password: String,
    val nickname: String
) {
    // Class body
}

// 5. Extension functions (if any)
fun User.isNew(): Boolean = id == null
```

### Formatting

#### Indentation
- **4 spaces** for indentation (no tabs)
- Continuation indent: 4 spaces

#### Line Length
- **Maximum 120 characters** per line
- Break long lines at logical points

#### Braces
```kotlin
// Good: Braces on same line
class User {
    fun greet() {
        println("Hello")
    }
}

// Bad: Braces on new line (K&R style preferred)
class User
{
    fun greet()
    {
        println("Hello")
    }
}
```

#### Whitespace
```kotlin
// Good: Space after control flow keywords
if (condition) {
    doSomething()
}

while (isRunning) {
    process()
}

// Good: Space around operators
val sum = a + b
val result = calculate(x, y)

// Good: No space before comma, space after comma
fun method(a: String, b: Int, c: Boolean)

// Good: Space before and after colon in type annotations
val name: String = "John"
fun method(param: Int): String
```

### Null Safety

```kotlin
// Prefer non-null types
val name: String = "John"

// Use nullable only when necessary
val optionalName: String? = null

// Safe call operator
val length = name?.length

// Elvis operator for defaults
val length = name?.length ?: 0

// Non-null assertion (use sparingly, only when certain)
val length = name!!.length  // Avoid if possible

// Let for null checks with operations
name?.let {
    println("Name is $it")
}
```

### Immutability

```kotlin
// Prefer val over var
val immutableValue = "constant"
var mutableValue = "changeable"  // Only if mutation is necessary

// Prefer immutable collections
val list = listOf(1, 2, 3)  // Immutable
val mutableList = mutableListOf(1, 2, 3)  // Only if mutation needed

// Data classes are immutable by default
data class User(val id: Long, val name: String)
```

### Functions

```kotlin
// Single-expression functions
fun double(x: Int): Int = x * 2

// Multi-line functions with explicit return type
fun calculateTotal(items: List<Item>): Money {
    val sum = items.sumOf { it.price.amount }
    return Money(sum)
}

// Default parameters
fun greet(name: String = "Guest"): String {
    return "Hello, $name"
}

// Named arguments for clarity
createUser(
    email = "user@example.com",
    password = "hashed",
    nickname = "John"
)
```

### Classes

```kotlin
// Primary constructor with properties
class User(
    val id: Long?,
    val email: Email,
    val password: String,
    val nickname: String
)

// Data classes for DTOs and value objects
data class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) { "Invalid email format" }
    }
}

// Class with initialization block
class UserDomainService(
    private val userRepository: UserRepository
) {
    init {
        // Initialization code
    }
}

// Companion object for factory methods and constants
class User(val id: Long?, val email: Email) {
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()

        fun create(email: String): User {
            return User(null, Email(email))
        }
    }
}
```

### When Expressions

```kotlin
// Prefer when over multiple if-else
fun describe(obj: Any): String = when (obj) {
    is String -> "String of length ${obj.length}"
    is Int -> "Integer: $obj"
    is List<*> -> "List with ${obj.size} items"
    else -> "Unknown type"
}

// When with subject
when (status) {
    Status.PENDING -> handlePending()
    Status.APPROVED -> handleApproved()
    Status.REJECTED -> handleRejected()
}

// When without subject (replaces if-else chain)
when {
    age < 18 -> "Minor"
    age < 65 -> "Adult"
    else -> "Senior"
}
```

### Collections

```kotlin
// Prefer functional operations
val names = users.map { it.name }
val adults = users.filter { it.age >= 18 }
val total = items.sumOf { it.price }

// Use sequence for large collections
val result = items.asSequence()
    .filter { it.isActive }
    .map { it.process() }
    .toList()

// List comprehension style
val squares = (1..10).map { it * it }
```

### String Templates

```kotlin
// Simple variable interpolation
val name = "John"
println("Hello, $name")

// Expression interpolation
println("Sum: ${a + b}")

// Property access
println("User email: ${user.email.value}")

// Multi-line strings
val json = """
    {
        "name": "$name",
        "age": $age
    }
""".trimIndent()
```

## Package Structure

### Module Organization

```
koreatech.kapp/
├── domain/              # Core module (no subpackage "core")
│   ├── common/          # Shared domain primitives
│   ├── user/            # User bounded context
│   │   ├── model/       # Domain models (entities, value objects)
│   │   ├── repository/  # Repository interfaces
│   │   └── service/     # Domain services
│   └── meal/            # Meal bounded context
│       ├── model/
│       ├── repository/
│       └── service/
│
├── persistence/         # Infra module (no subpackage "infra")
│   ├── user/            # User persistence
│   │   ├── DBUserRepository.kt
│   │   ├── UserEntity.kt
│   │   └── UserTable.kt
│   └── meal/            # Meal persistence
│
└── (api module)/        # API module
    ├── config/          # Configuration classes
    ├── global/          # Cross-cutting concerns
    ├── auth/            # Authentication infrastructure
    ├── user/            # User feature
    │   ├── controller/
    │   │   ├── AuthController.kt
    │   │   └── dto/
    │   │       └── UserDtos.kt
    │   ├── service/
    │   │   └── AuthService.kt
    │   └── jwt/
    └── meal/            # Meal feature
        ├── controller/
        ├── service/
        └── dto/
```

### Package Naming Rules
- **All lowercase**: No camelCase or PascalCase in package names
- **Singular nouns**: `user`, not `users`
- **No abbreviations**: `authentication`, not `auth` (exception: widely-known acronyms like `jwt`)
- **Hierarchical**: Organize by feature/bounded context

## Naming Conventions

### Classes and Interfaces

```kotlin
// PascalCase for class names
class User
class UserDomainService
class DBUserRepository

// Descriptive names
class AuthController  // Not: UC (unclear)
class MealResponse    // Not: MR (unclear)

// Interfaces: No "I" prefix
interface UserRepository  // Not: IUserRepository

// Abstract classes: Can use "Abstract" prefix if needed
abstract class AbstractRepository

// Exception classes: End with "Exception"
class UserNotFoundException : DomainException()

// Test classes: Class name + "Test"
class UserDomainServiceTest
```

### Functions and Variables

```kotlin
// camelCase for functions and variables
fun calculateTotal(): Money
val userName: String
var isActive: Boolean

// Boolean names: Use "is", "has", "can", etc.
val isValid = true
val hasPermission = checkPermission()
fun canAccess(): Boolean

// Avoid ambiguous names
fun fetchUser()  // Good: clear action
fun user()       // Bad: unclear

// Use verb for functions
fun saveUser()
fun deleteOrder()
fun validateEmail()

// Use noun for properties
val userName: String
val totalPrice: Money
```

### Constants

```kotlin
// UPPER_SNAKE_CASE for constants
const val MAX_RETRY_COUNT = 3
const val DEFAULT_PAGE_SIZE = 20

// Companion object constants
companion object {
    private const val EMAIL_REGEX_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$"
    const val MIN_PASSWORD_LENGTH = 8
}

// Configuration properties: camelCase
@ConfigurationProperties("jwt")
data class JwtProperties(
    val secret: String,
    val expirationMs: Long
)
```

### Generic Type Parameters

```kotlin
// Single letter for simple generics
class Box<T>(val value: T)
fun <T> List<T>.second(): T

// Descriptive names for complex cases
class Repository<Entity, Id>(
    private val entityClass: Class<Entity>
)

// Standard conventions
// T: Type
// E: Element
// K: Key
// V: Value
// R: Result/Return type
```

## Code Organization

### Class Member Ordering

```kotlin
class UserService(
    // 1. Constructor parameters
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    // 2. Companion object
    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
        private const val MAX_LOGIN_ATTEMPTS = 3
    }

    // 3. Properties (in order of importance)
    private val cache = ConcurrentHashMap<Long, User>()
    var maxRetries: Int = 3

    // 4. Init blocks
    init {
        logger.info("UserService initialized")
    }

    // 5. Public methods
    fun register(email: String, password: String): User {
        // Implementation
    }

    fun login(email: String, password: String): User {
        // Implementation
    }

    // 6. Private methods
    private fun validateEmail(email: String) {
        // Implementation
    }

    private fun hashPassword(password: String): String {
        // Implementation
    }
}
```

### File Organization

- **One public class per file** (preferred)
- **File name matches class name**: `User.kt` contains `User` class
- **Multiple related classes**: OK for small, tightly-coupled classes (DTOs, sealed classes)

```kotlin
// UserDtos.kt - Multiple related DTOs in one file
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String
)
```

### Import Organization

```kotlin
// 1. Standard library imports
import kotlin.math.abs

// 2. Third-party imports (alphabetical)
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

// 3. Project imports (alphabetical by package)
import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.repository.UserRepository

// Use wildcard imports sparingly (only for related groups)
import koreatech.kapp.domain.user.model.*  // OK if importing many from same package
```

## Domain-Driven Design Conventions

### Aggregate Roots

```kotlin
// Aggregate root: Entity with identity and lifecycle
class User(
    val id: Long?,                    // Identity
    val email: Email,                 // Value object
    val password: String,             // Primitive (hashed)
    val nickname: String
) {
    // Business logic methods
    fun changeNickname(newNickname: String): User {
        require(newNickname.isNotBlank()) { "Nickname cannot be blank" }
        return copy(nickname = newNickname)
    }

    // Factory methods in companion object
    companion object {
        fun register(email: Email, hashedPassword: String, nickname: String): User {
            return User(
                id = null,
                email = email,
                password = hashedPassword,
                nickname = nickname
            )
        }
    }
}
```

### Value Objects

```kotlin
// Value object: Immutable, validated, no identity
data class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) { "Invalid email format: $value" }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
    }
}

data class Money(val amount: Int) {
    init {
        require(amount >= 0) { "Money amount cannot be negative: $amount" }
    }

    operator fun plus(other: Money): Money = Money(amount + other.amount)
    operator fun times(multiplier: Int): Money = Money(amount * multiplier)
}

// Always use data class for value objects
// Override equals/hashCode automatically
// Immutable by default
```

### Repository Interfaces

```kotlin
// Repository interface in domain layer
interface UserRepository {
    fun save(user: User): User
    fun findById(id: Long): User?
    fun findByEmail(email: Email): User?
    fun existsByEmail(email: Email): Boolean
}

// Naming conventions:
// - save: Insert or update
// - find: Return nullable result
// - get: Return non-null or throw exception
// - exists: Return boolean
// - delete: Remove entity
```

### Domain Services

```kotlin
// Domain service: Orchestrates complex operations
class UserDomainService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder  // Infra concern, interface in domain
) {
    fun registerUser(email: Email, rawPassword: String, nickname: String): User {
        // Business rule: Email must be unique
        if (userRepository.existsByEmail(email)) {
            throw DomainException("Email already exists: ${email.value}")
        }

        // Create user with hashed password
        val hashedPassword = passwordEncoder.encode(rawPassword)
        val user = User.register(email, hashedPassword, nickname)

        // Persist and return
        return userRepository.save(user)
    }
}

// Domain services contain business logic that:
// - Doesn't naturally fit in an entity
// - Spans multiple aggregates
// - Requires external dependencies (repositories)
```

### Domain Exceptions

```kotlin
// Base domain exception
open class DomainException(message: String) : RuntimeException(message)

// Specific exceptions
class UserNotFoundException(userId: Long) : DomainException("User not found: $userId")
class EmailAlreadyExistsException(email: String) : DomainException("Email already exists: $email")
class InvalidPasswordException : DomainException("Invalid password")

// Throw at appropriate layer
// - Value objects: IllegalArgumentException for invalid construction
// - Domain services: DomainException for business rule violations
// - API layer: Translate to HTTP status codes
```

## Spring Framework Conventions

### Dependency Injection

```kotlin
// Constructor injection (preferred)
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    // Use injected dependencies
}

// Field injection (avoid)
@Service
class AuthService {
    @Autowired  // Avoid: harder to test, hides dependencies
    private lateinit var userRepository: UserRepository
}
```

### Bean Configuration

```kotlin
// Java Config style with @Bean
@Configuration
class UserBeanConfig {

    @Bean
    fun userDomainService(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): UserDomainService {
        return UserDomainService(userRepository, passwordEncoder)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(10)
    }
}

// Component scanning for Spring-managed beans
@Service  // For services
@Repository  // For repositories
@Controller  // For controllers
@Configuration  // For config classes
```

### Controllers

```kotlin
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    // Use HTTP method annotations
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): UserResponse {
        return authService.register(request)
            .let { UserResponse.from(it) }
    }

    // Explicit mapping and validation
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: Long): UserResponse {
        return authService.getUser(id)
            .let { UserResponse.from(it) }
    }
}

// Controller conventions:
// - Thin controllers: delegate to services
// - Use DTOs for request/response
// - Validate input with @Valid
// - Return appropriate HTTP status codes
```

### Exception Handling

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
        return ResponseEntity.badRequest().body(error)
    }

    // Handle specific exceptions with appropriate status codes
}
```

## Database Conventions

### Table Naming

```sql
-- Use plural, lowercase with underscores
users          -- Good
user           -- Bad (singular)
Users          -- Bad (capitalized)
user_accounts  -- Good (multi-word)
```

### Column Naming

```sql
-- Use lowercase with underscores
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Avoid camelCase in database
-- Use snake_case instead
```

### JooQ Conventions

```kotlin
// Table object: Singular + "Table"
object UserTable : Table<UserRecord>("users") {
    val ID = createField("id", SQLDataType.BIGINT)
    val EMAIL = createField("email", SQLDataType.VARCHAR(255))
}

// Entity: Singular + "Entity"
data class UserEntity(
    val id: Long?,
    val email: String,
    val password: String
) {
    fun toDomain(): User = User(id, Email(email), password)

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            email = user.email.value,
            password = user.password
        )
    }
}

// Repository implementation: "DB" + Domain + "Repository"
@Repository
class DBUserRepository(
    private val dsl: DSLContext
) : UserRepository {
    override fun save(user: User): User {
        // Implementation
    }
}
```

## API Conventions

### REST Endpoints

```
# Resource-based URLs (nouns, not verbs)
POST   /api/users               # Create user
GET    /api/users               # List users
GET    /api/users/{id}          # Get specific user
PUT    /api/users/{id}          # Update entire user
PATCH  /api/users/{id}          # Partial update
DELETE /api/users/{id}          # Delete user

# Nested resources
GET    /api/users/{id}/orders   # Get user's orders

# Use query params for filtering
GET    /api/meals?category=lunch&maxPrice=10000
```

### DTOs

```kotlin
// Request DTOs: Verb/Action + "Request"
data class RegisterRequest(
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 8)
    val password: String,

    @field:NotBlank
    val nickname: String
)

// Response DTOs: Resource + "Response"
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id ?: throw IllegalStateException("User ID is null"),
            email = user.email.value,
            nickname = user.nickname
        )
    }
}

// Group related DTOs in one file: UserDtos.kt, MealDtos.kt
```

### HTTP Status Codes

```kotlin
// Use appropriate status codes
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // 201 for resource creation
fun create(@RequestBody request: CreateRequest): Response

@GetMapping("/{id}")
fun get(@PathVariable id: Long): Response  // 200 OK (default)

@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)  // 204 for successful deletion
fun delete(@PathVariable id: Long)

// Error status codes via exception handler
// 400 Bad Request: Validation errors
// 401 Unauthorized: Authentication required
// 403 Forbidden: Insufficient permissions
// 404 Not Found: Resource not found
// 500 Internal Server Error: Unexpected errors
```

## Testing Conventions

### Test Class Naming

```kotlin
// Unit test: ClassName + "Test"
class UserDomainServiceTest

// Integration test: ClassName + "IntegrationTest"
class DBUserRepositoryIntegrationTest

// E2E test: Feature + "E2ETest"
class UserRegistrationE2ETest
```

### Test Method Naming

```kotlin
class UserDomainServiceTest {
    // Use backticks for descriptive test names
    @Test
    fun `should register user with valid email`() {
        // Test implementation
    }

    @Test
    fun `should throw exception when email already exists`() {
        // Test implementation
    }

    @Test
    fun `should hash password before saving user`() {
        // Test implementation
    }
}

// Template: should [expected behavior] when [condition]
// Or: should [expected behavior] given [context]
```

### Test Structure (Given-When-Then)

```kotlin
@Test
fun `should register user successfully`() {
    // Given: Setup test data and mocks
    val email = Email("test@example.com")
    val password = "password123"
    every { userRepository.existsByEmail(email) } returns false
    every { userRepository.save(any()) } answers { firstArg() }

    // When: Execute the behavior under test
    val user = userDomainService.registerUser(email, password, "Test User")

    // Then: Verify the results
    assertThat(user.email).isEqualTo(email)
    assertThat(user.nickname).isEqualTo("Test User")
    verify { userRepository.save(any()) }
}
```

### Assertion Libraries

```kotlin
// Use AssertJ for fluent assertions
import org.assertj.core.api.Assertions.*

assertThat(user.email.value).isEqualTo("test@example.com")
assertThat(user.id).isNotNull()
assertThat(users).hasSize(3)
assertThat(users).extracting("email").contains("test@example.com")

// For exceptions
assertThatThrownBy {
    userService.register("invalid-email", "password")
}
    .isInstanceOf(IllegalArgumentException::class.java)
    .hasMessageContaining("Invalid email")
```

### Mocking

```kotlin
// Use MockK for Kotlin
import io.mockk.*

// Create mock
val repository = mockk<UserRepository>()

// Stub behavior
every { repository.findById(1L) } returns user
every { repository.save(any()) } answers { firstArg() }

// Verify calls
verify { repository.save(user) }
verify(exactly = 1) { repository.findById(1L) }
verify { repository wasNot Called }
```

## Documentation Standards

### KDoc Comments

```kotlin
/**
 * Represents a user in the system.
 *
 * Users are aggregate roots in the user bounded context.
 * Each user has a unique email address and can authenticate using a password.
 *
 * @property id Unique identifier, null for new users before persistence
 * @property email User's email address (validated)
 * @property password BCrypt-hashed password (never stored in plain text)
 * @property nickname Display name for the user
 */
class User(
    val id: Long?,
    val email: Email,
    val password: String,
    val nickname: String
)

/**
 * Registers a new user with the provided credentials.
 *
 * @param email User's email address (must be unique)
 * @param rawPassword Plain text password (will be hashed)
 * @param nickname User's display name
 * @return The created user with generated ID
 * @throws DomainException if email already exists
 */
fun registerUser(email: Email, rawPassword: String, nickname: String): User
```

### Code Comments

```kotlin
// Use comments to explain "why", not "what"

// Good: Explains reasoning
// Hash password before storage to meet security requirements
val hashedPassword = passwordEncoder.encode(rawPassword)

// Bad: States the obvious
// Set the user's nickname
user.nickname = newNickname

// Good: Explains business rule
// Email must be unique to prevent duplicate accounts
if (userRepository.existsByEmail(email)) {
    throw EmailAlreadyExistsException(email.value)
}

// Use TODO comments for temporary code
// TODO: Add rate limiting to prevent brute force attacks
fun login(email: String, password: String): User

// Use FIXME for known issues
// FIXME: This validation is too permissive, needs stricter rules
fun validatePassword(password: String): Boolean
```

### README Files

Each module should have a SPEC.md file documenting:
- Purpose and responsibilities
- Key components
- Development guidelines
- Examples

## Git Conventions

### Branch Naming

```
main                           # Production-ready code
develop                        # Integration branch

feature/user-authentication    # New features
feature/meal-management

fix/login-validation-bug       # Bug fixes
fix/password-encoding

refactor/repository-layer      # Code refactoring

docs/api-documentation         # Documentation updates

test/integration-tests         # Test additions
```

### Commit Messages

Follow Conventional Commits:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring (no behavior change)
- `docs`: Documentation changes
- `test`: Add or update tests
- `chore`: Maintenance tasks (dependencies, build config)
- `style`: Code style changes (formatting, no logic change)
- `perf`: Performance improvements

**Examples**:
```
feat(auth): add JWT token authentication

Implement JWT-based authentication with token generation
and validation. Includes bearer token support in API endpoints.

Closes #123

---

fix(user): validate email format on registration

Add email format validation to prevent invalid emails
from being saved to the database.

---

refactor(repository): migrate from JPA to JooQ

Replace JPA repository implementations with type-safe
JooQ queries for better SQL control and performance.

Breaking change: Repository interface signatures changed.

---

docs(architecture): add system architecture documentation

Create ARCHITECTURE.md with detailed system design,
data flow diagrams, and module specifications.
```

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed code
- [ ] Commented complex code
- [ ] Updated documentation
- [ ] No new warnings
- [ ] Tests pass locally
```

## Code Review Checklist

### Before Submitting PR
- [ ] Code compiles without warnings
- [ ] All tests pass
- [ ] Code follows conventions in this document
- [ ] Added tests for new functionality
- [ ] Updated documentation
- [ ] No debug code or commented-out code
- [ ] No hardcoded values (use configuration)
- [ ] Proper error handling
- [ ] Commit messages follow conventions

### During Code Review
- [ ] Code is readable and maintainable
- [ ] Business logic is in appropriate layer
- [ ] No code duplication
- [ ] Proper separation of concerns
- [ ] Security considerations addressed
- [ ] Performance considerations addressed
- [ ] Test coverage is adequate
- [ ] API contracts are backward compatible

## IDE Configuration

### IntelliJ IDEA Settings

**Code Style (Kotlin)**:
- Indentation: 4 spaces
- Continuation indent: 4 spaces
- Line length: 120 characters
- Imports: Alphabetical, no wildcard imports (except stdlib)

**Inspections**:
- Enable Kotlin inspections
- Treat warnings as errors
- Enable nullability inspections
- Enable unused code detection

**EditorConfig** (.editorconfig):
```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_size = 4
indent_style = space
max_line_length = 120

[*.{yml,yaml}]
indent_size = 2
indent_style = space
```

## Kotlin Specific Best Practices

### Scope Functions

```kotlin
// let: For null checks and transformations
val length = str?.let { it.length } ?: 0

user?.let { u ->
    println("User: ${u.name}")
}

// apply: For object configuration
val user = User(id, email, password).apply {
    this.nickname = "John"
    this.isActive = true
}

// also: For side effects
val result = calculate().also {
    logger.debug("Result: $it")
}

// run: For executing block and returning result
val result = run {
    val a = complex()
    val b = calculation()
    a + b
}

// with: For calling multiple methods on object
with(user) {
    println(name)
    println(email)
    save()
}
```

### Extension Functions

```kotlin
// Add functionality to existing types
fun String.isValidEmail(): Boolean {
    return matches("^[A-Za-z0-9+_.-]+@(.+)$".toRegex())
}

fun User.isNew(): Boolean = id == null

// Use sparingly, don't pollute standard types
```

### Sealed Classes

```kotlin
// For representing restricted class hierarchies
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Exhaustive when expressions
fun handleResult(result: Result<User>) = when (result) {
    is Result.Success -> println("User: ${result.data}")
    is Result.Error -> println("Error: ${result.message}")
    Result.Loading -> println("Loading...")
}
```

### Type Aliases

```kotlin
// For improving readability
typealias UserId = Long
typealias EmailAddress = String
typealias ValidationResult = Result<Unit>

fun findUser(id: UserId): User?
```

## Security Best Practices

### Input Validation
```kotlin
// Always validate at boundaries (API layer)
@PostMapping("/register")
fun register(@Valid @RequestBody request: RegisterRequest): UserResponse

// Validate in domain layer (value objects)
data class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) { "Invalid email" }
    }
}
```

### Password Handling
```kotlin
// Never log passwords
logger.info("User login attempt: ${request.email}")  // Good
logger.info("User login: ${request.password}")       // BAD!

// Always hash passwords
val hashedPassword = passwordEncoder.encode(rawPassword)

// Use constant-time comparison for passwords
passwordEncoder.matches(rawPassword, hashedPassword)
```

### SQL Injection Prevention
```kotlin
// JooQ handles parameterization automatically
dsl.selectFrom(UserTable)
    .where(UserTable.EMAIL.eq(email))  // Safe, parameterized
    .fetchOne()

// Never concatenate user input into SQL
// dsl.execute("SELECT * FROM users WHERE email = '$email'")  // NEVER DO THIS!
```

---

**Last Updated**: 2025-11-13
**Version**: 1.0.0

For questions or clarifications about these conventions, please contact the development team or create an issue.
