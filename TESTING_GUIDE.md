# Testing Guide

이 문서는 k-app 프로젝트의 테스트 작성 및 실행 가이드입니다.

## 📋 목차

1. [테스트 구조](#테스트-구조)
2. [테스트 실행](#테스트-실행)
3. [테스트 작성 가이드](#테스트-작성-가이드)
4. [Coverage 확인](#coverage-확인)
5. [CI/CD 통합](#cicd-통합)

---

## 테스트 구조

### 레이어별 테스트

프로젝트는 Clean Architecture를 따르며, 각 레이어별로 적절한 테스트가 작성되어 있습니다:

```
k-app/
├── core/src/test/          # 도메인 레이어 단위 테스트
│   ├── domain/common/      # Value Objects 테스트
│   ├── domain/user/        # User 도메인 테스트
│   └── domain/meal/        # Meal 도메인 테스트
├── infra/src/test/         # 인프라 레이어 통합 테스트
│   └── persistence/        # Repository 통합 테스트
└── api/src/test/           # API 레이어 통합 테스트
    ├── user/controller/    # Auth API 테스트
    └── meal/controller/    # Meal API 테스트
```

### 테스트 통계

- **Core Layer**: 74개 단위 테스트
- **Infrastructure Layer**: 11개 통합 테스트
- **API Layer**: 12개 통합 테스트
- **Total**: 97+ 테스트

---

## 테스트 실행

### 전체 테스트 실행

```bash
# 모든 모듈의 테스트 실행
./gradlew test

# 특정 모듈만 실행
./gradlew :core:test
./gradlew :infra:test
./gradlew :api:test
```

### Coverage 포함 테스트 실행

```bash
# 테스트 실행 및 coverage 리포트 생성
./gradlew test jacocoTestReport

# Coverage 검증 (최소 60% 요구)
./gradlew jacocoTestCoverageVerification
```

### 특정 테스트만 실행

```bash
# 특정 테스트 클래스 실행
./gradlew :core:test --tests "EmailTest"

# 특정 테스트 메서드 실행
./gradlew :core:test --tests "EmailTest.유효한 이메일 주소로 Email 객체를 생성할 수 있다"

# 패턴 매칭으로 여러 테스트 실행
./gradlew test --tests "*Controller*"
```

### 빠른 피드백을 위한 옵션

```bash
# 병렬 실행으로 속도 향상
./gradlew test --parallel

# 실패한 테스트만 재실행
./gradlew test --rerun-tasks

# 상세한 테스트 결과 출력
./gradlew test --info
```

---

## 테스트 작성 가이드

### 1. Core Layer - 단위 테스트

**Value Objects 테스트 예시**:

```kotlin
class EmailTest {
    @Test
    fun `유효한 이메일 주소로 Email 객체를 생성할 수 있다`() {
        // given
        val validEmail = "user@example.com"

        // when
        val email = Email(validEmail)

        // then
        assertEquals(validEmail, email.value)
    }

    @Test
    fun `유효하지 않은 이메일 형식은 예외를 발생시킨다`() {
        // given
        val invalidEmail = "invalid-email"

        // when & then
        assertThrows<IllegalArgumentException> {
            Email(invalidEmail)
        }
    }
}
```

**Domain Service 테스트 예시** (MockK 사용):

```kotlin
class UserDomainServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userDomainService: UserDomainService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        userDomainService = UserDomainService(userRepository, passwordEncoder)
    }

    @Test
    fun `새로운 사용자를 생성할 수 있다`() {
        // given
        val email = Email("user@example.com")
        val rawPassword = "password123"

        every { userRepository.existsByEmail(email) } returns false
        every { passwordEncoder.encode(rawPassword) } returns "hashed"
        every { userRepository.save(any()) } returns savedUser

        // when
        val result = userDomainService.createUser(email, rawPassword, "홍길동", "2024001")

        // then
        assertNotNull(result.id)
        verify(exactly = 1) { userRepository.save(any()) }
    }
}
```

### 2. Infrastructure Layer - 통합 테스트

**Repository 테스트 예시**:

```kotlin
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DBUserRepositoryTest {

    @Autowired
    private lateinit var dsl: DSLContext

    private lateinit var repository: DBUserRepository

    @AfterEach
    fun cleanup() {
        dsl.deleteFrom(UserTable).execute()
    }

    @Test
    fun `새로운 사용자를 저장할 수 있다`() {
        // given
        val user = User.create(...)

        // when
        val savedUser = repository.save(user)

        // then
        assertNotNull(savedUser.id)
    }
}
```

### 3. API Layer - 통합 테스트

**Controller 테스트 예시**:

```kotlin
@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Test
    fun `회원가입 API 테스트 - 성공`() {
        // given
        val request = RegisterRequest(...)
        every { authService.register(any()) } returns response

        // when & then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
    }
}
```

### 테스트 작성 Best Practices

1. **Given-When-Then 패턴 사용**
   ```kotlin
   // given: 테스트 준비
   // when: 테스트 실행
   // then: 결과 검증
   ```

2. **의미있는 테스트 이름**
   ```kotlin
   // ✅ Good
   fun `유효하지 않은 이메일 형식은 예외를 발생시킨다`()

   // ❌ Bad
   fun test1()
   ```

3. **하나의 테스트는 하나의 동작만 검증**
   ```kotlin
   // ✅ Good: 하나의 동작만 테스트
   @Test
   fun `사용자 이름을 업데이트할 수 있다`()

   // ❌ Bad: 여러 동작을 한번에 테스트
   @Test
   fun `사용자를 생성하고_조회하고_업데이트하고_삭제한다`()
   ```

4. **Edge Cases 테스트**
   ```kotlin
   @Test
   fun `빈 문자열로 생성하면 예외가 발생한다`()

   @Test
   fun `null 값으로 생성하면 예외가 발생한다`()

   @Test
   fun `경계값에서 올바르게 동작한다`()
   ```

---

## Coverage 확인

### HTML 리포트 생성

```bash
# Coverage 리포트 생성
./gradlew jacocoTestReport

# 리포트 확인
# Core: core/build/reports/jacoco/test/html/index.html
# Infra: infra/build/reports/jacoco/test/html/index.html
# API: api/build/reports/jacoco/test/html/index.html
```

### Coverage 요구사항

현재 설정된 최소 coverage:

- **Overall Coverage**: 60%
- **Class Coverage**: 50%
- **PR Changed Files**: 70%

### Coverage 향상 전략

1. **우선순위가 높은 코드부터 테스트**
   - 비즈니스 로직
   - 도메인 모델
   - Critical path

2. **Low hanging fruits 공략**
   - 간단한 getter/setter
   - Utility 함수
   - Value objects

3. **Integration tests 추가**
   - End-to-end scenarios
   - 실제 사용 케이스

---

## CI/CD 통합

### GitHub Actions Workflows

프로젝트에는 3개의 테스트 관련 워크플로우가 있습니다:

1. **ci.yml** - 일반 CI (모든 브랜치)
2. **test-required.yml** - PR 필수 테스트 (main, develop)
3. **pr-checks.yml** - PR 검증

### PR 생성 시 자동 실행

PR을 생성하면 자동으로:

1. ✅ 모든 테스트 실행
2. ✅ Coverage 리포트 생성
3. ✅ PR에 결과 코멘트
4. ✅ 테스트 실패 시 merge 차단

### Branch Protection Rules

테스트가 통과해야만 merge 가능하도록 설정:

1. Settings → Branches → Add rule
2. Branch name pattern: `main`
3. ✅ Require status checks to pass before merging
4. Required checks:
   - Test Gate (Required for Merge)
   - Test Summary
   - Test Results (Required)

자세한 설정 방법: [BRANCH_PROTECTION_GUIDE.md](.github/BRANCH_PROTECTION_GUIDE.md)

---

## 문제 해결

### 일반적인 문제

#### Q: 테스트가 로컬에서는 통과하는데 CI에서 실패합니다

**A**: 환경 차이를 확인하세요:
1. 데이터베이스 상태 초기화 확인
2. 타임존 설정 확인
3. 의존성 버전 확인
4. `@AfterEach`로 테스트 후 cleanup 수행

#### Q: 테스트가 간헐적으로 실패합니다 (Flaky tests)

**A**: 다음을 확인하세요:
1. 비동기 코드의 적절한 대기
2. 랜덤 데이터 사용 여부
3. 시간 의존적인 코드
4. 테스트 간 의존성

#### Q: Coverage가 낮게 나옵니다

**A**:
1. 중요한 비즈니스 로직부터 테스트 추가
2. Branch coverage 확인
3. Exception handling 테스트
4. Edge cases 테스트

---

## 참고 자료

### 테스트 프레임워크

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [MockK Documentation](https://mockk.io/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)

### Testing Best Practices

- [Test Driven Development](https://en.wikipedia.org/wiki/Test-driven_development)
- [Unit Testing Best Practices](https://docs.microsoft.com/en-us/dotnet/core/testing/unit-testing-best-practices)
- [Integration Testing](https://martinfowler.com/bliki/IntegrationTest.html)

### Coverage Tools

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Code Coverage Best Practices](https://testing.googleblog.com/2020/08/code-coverage-best-practices.html)

---

**작성일**: 2025-11-13
**버전**: 1.0.0
**최종 업데이트**: 테스트 추가 시 이 문서를 업데이트하세요
