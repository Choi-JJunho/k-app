# Core Module Specification

## Overview
The **Core Module** contains pure domain logic following Domain-Driven Design (DDD) principles. This module has **NO external framework dependencies** and represents the heart of the business logic.

## Technology Stack
- Kotlin 1.9.25
- Java 21
- Pure domain logic (no Spring, no JPA, no external frameworks)

## Architecture Principles
- **Framework Independent**: No Spring or external framework dependencies
- **Persistence Ignorant**: No database or ORM concerns
- **Business Logic First**: All business rules and invariants enforced here
- **Immutability Preferred**: Value objects are immutable
- **Fail-Fast Validation**: Domain exceptions thrown immediately on invalid state

## Directory Structure

```
core/
└── src/main/kotlin/koreatech/kapp/domain/
    ├── common/                    # Shared domain primitives
    │   ├── DomainException.kt     # Base domain exception
    │   ├── Email.kt               # Email value object
    │   └── Money.kt               # Money value object
    ├── user/                      # User bounded context
    │   ├── model/
    │   │   └── User.kt            # User aggregate root
    │   ├── repository/
    │   │   └── UserRepository.kt  # User repository interface
    │   └── service/
    │       └── UserDomainService.kt # User domain service
    └── meal/                      # Meal bounded context
        ├── model/
        │   ├── Meal.kt            # Meal aggregate root
        │   ├── Calories.kt        # Calories value object
        │   └── Menu.kt            # Menu value object
        ├── repository/
        │   └── MealRepository.kt  # Meal repository interface
        └── service/
            └── MealDomainService.kt # Meal domain service
```

## Domain Model Specifications

### 1. Common Domain Primitives

#### Email Value Object
**Purpose**: Validate and encapsulate email addresses

**Specifications**:
- Must match email regex pattern: `^[A-Za-z0-9+_.-]+@(.+)$`
- Immutable value object
- Throws `IllegalArgumentException` on invalid format
- Use for all email representations in the domain

**Example Usage**:
```kotlin
val email = Email("user@example.com")  // Valid
val invalid = Email("not-an-email")    // Throws exception
```

#### Money Value Object
**Purpose**: Represent monetary values with precision

**Specifications**:
- Must be non-negative (`amount >= 0`)
- Uses `Int` for amount (consider currency precision needs)
- Immutable value object
- Throws `IllegalArgumentException` for negative amounts
- Override `equals()` and `hashCode()` for value semantics

**Example Usage**:
```kotlin
val price = Money(5000)      // Valid
val invalid = Money(-100)    // Throws exception
```

#### DomainException
**Purpose**: Base exception for all domain-level errors

**Specifications**:
- Extends `RuntimeException`
- Used for business rule violations
- Should contain descriptive error messages
- Caught and translated to HTTP responses in API layer

### 2. User Bounded Context

#### User Aggregate Root
**Purpose**: Represent a user entity with authentication capabilities

**Specifications**:
- **ID**: `Long?` (nullable for new users before persistence)
- **Email**: `Email` value object (unique identifier)
- **Password**: `String` (should be hashed before persistence)
- **Nickname**: `String` (user display name)

**Invariants**:
- Email must be valid (enforced by Email value object)
- Password must not be empty (consider adding validation)
- Nickname must not be empty (consider adding validation)

**Business Rules**:
- Users are identified by email
- Password should be hashed before storage (handled in infra layer)
- User creation requires all fields

**Example**:
```kotlin
val user = User(
    id = null,
    email = Email("user@example.com"),
    password = "hashedPassword",
    nickname = "John Doe"
)
```

#### UserRepository Interface
**Purpose**: Abstract data access for User aggregate

**Specifications**:
- `save(user: User): User` - Persist or update user
- `findByEmail(email: Email): User?` - Find user by email
- `existsByEmail(email: Email): Boolean` - Check email existence

**Implementation Rules**:
- Implemented in infra layer
- Must handle ID generation for new users
- Must ensure email uniqueness
- Return null for non-existent users (not exceptions)

#### UserDomainService
**Purpose**: Orchestrate complex user domain operations

**Specifications**:
- `registerUser(email: Email, rawPassword: String, nickname: String): User`
  - Validates email uniqueness
  - Delegates password encoding to infra layer
  - Creates and saves new user
  - Throws exception if email already exists

**Business Rules**:
- Email must be unique across all users
- Password must be encoded before saving
- All fields required for registration

### 3. Meal Bounded Context

#### Calories Value Object
**Purpose**: Represent caloric content of meals

**Specifications**:
- Must be non-negative (`value >= 0`)
- Uses `Int` for calorie count
- Immutable value object
- Throws `IllegalArgumentException` for negative values

**Example**:
```kotlin
val calories = Calories(500)    // Valid
val invalid = Calories(-100)    // Throws exception
```

#### Menu Value Object
**Purpose**: Represent meal menu information

**Specifications**:
- Contains list of dish names
- Immutable value object
- Can be empty (represents no menu items)
- Use `List<String>` for menu items

**Example**:
```kotlin
val menu = Menu(listOf("Rice", "Kimchi", "Bulgogi"))
val emptyMenu = Menu(emptyList())
```

#### Meal Aggregate Root
**Purpose**: Represent a meal entity with nutritional information

**Specifications**:
- **ID**: `Long?` (nullable for new meals)
- **Name**: `String` (meal name/type)
- **Price**: `Money` value object
- **Calories**: `Calories` value object
- **Menu**: `Menu` value object

**Invariants**:
- Price must be non-negative (enforced by Money)
- Calories must be non-negative (enforced by Calories)
- Name must not be empty (consider adding validation)

**Business Rules**:
- Each meal has associated price, calories, and menu
- Meal data should be immutable after creation (consider making it a data class)

**Example**:
```kotlin
val meal = Meal(
    id = null,
    name = "Lunch Set A",
    price = Money(8000),
    calories = Calories(750),
    menu = Menu(listOf("Rice", "Soup", "Side dishes"))
)
```

#### MealRepository Interface
**Purpose**: Abstract data access for Meal aggregate

**Specifications**:
- `save(meal: Meal): Meal` - Persist or update meal
- `findById(id: Long): Meal?` - Find meal by ID
- `findAll(): List<Meal>` - Retrieve all meals

**Implementation Rules**:
- Implemented in infra layer
- Must handle ID generation for new meals
- Return null for non-existent meals
- findAll() returns empty list when no meals exist

#### MealDomainService
**Purpose**: Orchestrate complex meal domain operations

**Specifications**:
- `createMeal(name: String, price: Money, calories: Calories, menu: Menu): Meal`
  - Validates meal data
  - Creates and saves new meal
  - Returns persisted meal with ID

**Business Rules**:
- All meal fields are required
- Price and calories must be non-negative
- Menu can be empty

## Development Guidelines

### Adding New Domain Entities

1. **Create Aggregate Root**:
   - Place in `domain/{context}/model/`
   - Define clear boundaries
   - Enforce invariants in constructor
   - Use value objects for complex fields

2. **Define Repository Interface**:
   - Place in `domain/{context}/repository/`
   - Keep interface minimal
   - Return domain objects, not DTOs
   - Use nullable returns for optional results

3. **Create Domain Service (if needed)**:
   - Place in `domain/{context}/service/`
   - Use for operations spanning multiple aggregates
   - Keep logic framework-agnostic
   - Throw domain exceptions for violations

### Adding New Value Objects

1. **Create in `domain/common/`** (if shared) or in specific bounded context
2. **Requirements**:
   - Immutable (use `val`, not `var`)
   - Validate in constructor
   - Override `equals()` and `hashCode()`
   - Throw `IllegalArgumentException` for invalid values
   - Consider making it a data class

**Template**:
```kotlin
data class ValueObjectName(val value: Type) {
    init {
        require(validation) { "Error message" }
    }
}
```

### Domain Exception Guidelines

1. **Create Specific Exceptions**:
   - Extend `DomainException`
   - Use descriptive names (e.g., `UserAlreadyExistsException`)
   - Include helpful error messages
   - Place in same package as related domain objects

2. **When to Throw**:
   - Business rule violations
   - Invalid state transitions
   - Invariant violations
   - Validation failures

3. **When NOT to Throw**:
   - Technical failures (database, network) - handle in infra layer
   - Authentication/Authorization - handle in API layer
   - External service failures - handle in infra layer

## Testing Guidelines

### Unit Testing Requirements

1. **Test Value Objects**:
   - Valid construction
   - Invalid construction (expect exceptions)
   - Equality semantics
   - Edge cases

2. **Test Aggregate Roots**:
   - Invariant enforcement
   - Business rule validation
   - State transitions
   - Method behaviors

3. **Test Domain Services**:
   - Mock repository interfaces
   - Test business logic in isolation
   - Verify exception throwing
   - Test edge cases and boundaries

### Example Test Structure

```kotlin
class UserDomainServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userDomainService: UserDomainService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        userDomainService = UserDomainService(userRepository)
    }

    @Test
    fun `should register new user successfully`() {
        // Given
        val email = Email("test@example.com")
        every { userRepository.existsByEmail(email) } returns false
        every { userRepository.save(any()) } answers { firstArg() }

        // When
        val user = userDomainService.registerUser(email, "password", "Test User")

        // Then
        assertThat(user.email).isEqualTo(email)
        assertThat(user.nickname).isEqualTo("Test User")
    }

    @Test
    fun `should throw exception when email already exists`() {
        // Given
        val email = Email("existing@example.com")
        every { userRepository.existsByEmail(email) } returns true

        // When & Then
        assertThrows<DomainException> {
            userDomainService.registerUser(email, "password", "Test User")
        }
    }
}
```

## Design Patterns Used

1. **Aggregate Pattern**: User and Meal as aggregate roots
2. **Repository Pattern**: Abstract data access through interfaces
3. **Value Object Pattern**: Email, Money, Calories, Menu
4. **Domain Service Pattern**: UserDomainService, MealDomainService
5. **Fail-Fast Pattern**: Validation in constructors

## Future Enhancements

### Recommended Additions

1. **Enhanced Validation**:
   - Password strength validation in User
   - Nickname length constraints
   - Meal name validation

2. **Domain Events**:
   - UserRegisteredEvent
   - MealCreatedEvent
   - Publish events for cross-aggregate operations

3. **Additional Value Objects**:
   - PhoneNumber
   - Address
   - Username (separate from Email)

4. **Audit Fields**:
   - createdAt
   - updatedAt
   - createdBy
   - Consider using a base Auditable interface

5. **Soft Delete Support**:
   - deletedAt field
   - isDeleted flag
   - Filter deleted entities in repositories

## Dependencies

### Current Dependencies
None - Core module is dependency-free (except Kotlin stdlib)

### Allowed Dependencies
- Kotlin Standard Library
- JUnit/Mockk for testing
- No Spring or external frameworks

### Forbidden Dependencies
- Spring Framework
- JPA/Hibernate
- Database drivers
- Web frameworks
- External service clients

## Integration Points

### With Infra Module
- Infra implements repository interfaces
- Infra provides password encoding
- Infra handles persistence concerns

### With API Module
- API uses domain services for business operations
- API translates domain exceptions to HTTP responses
- API maps DTOs to/from domain objects

## Review Checklist

Before committing changes to core module:
- [ ] No external framework dependencies added
- [ ] All value objects are immutable
- [ ] Invariants validated in constructors
- [ ] Domain exceptions used appropriately
- [ ] Repository interfaces follow conventions
- [ ] Unit tests cover business logic
- [ ] Code follows Kotlin conventions
- [ ] Documentation updated for new features
