# K-App Project Specification

## 1. Overview

- Project: K-App backend
- Language: Kotlin 1.9.25
- Runtime: Java 21
- Build: Gradle (Kotlin DSL)
- Architecture: Multi-module Hexagonal/Clean Architecture

K-App는 사용자 인증과 식단 조회 기능을 제공하는 백엔드 서비스다.

## 2. Module Architecture

### 2.1 Modules

- `core`: 도메인 모델/도메인 서비스/포트(Repository, PasswordEncoder)
- `infra`: 영속성 어댑터(JOOQ), 보안 어댑터(BCrypt)
- `api`: 컨트롤러, 애플리케이션 서비스, 인증 리졸버, 예외 매핑
- `kapp-config`: 환경별 설정 서브모듈

### 2.2 Dependency Direction

- Compile-time
  - `api -> core`
  - `infra -> core`
- Runtime wiring
  - `api --(runtimeOnly)--> infra`

`api`는 `infra` 구현체를 직접 참조하지 않고, `core`의 인터페이스(포트)만 참조한다.

## 3. Domain Model

### 3.1 User Context

- Aggregate: `User`
- Value Objects: `Email`, `HashedPassword`, `UserId`
- Rules
  - 이메일 형식 유효성 검사
  - 이메일 중복 금지
  - 이름/학번(사번) 공백 금지
  - 비밀번호는 해시로 저장

### 3.2 Meal Context

- Aggregate: `Meal`
- Value Objects: `Money`, `Calories`, `Menu`
- Rules
  - 식당명 공백 금지
  - 메뉴 최소 1개
  - 칼로리 0~9999
  - 식단 날짜는 현재 기준 7일 이후 금지

## 4. Implemented APIs

### 4.1 Auth APIs

- `POST /api/auth/register`
  - Request: `email`, `password`, `name`, `studentEmployeeId`
  - Response: `RegisterResponse { user, message }`
- `POST /api/auth/login`
  - Request: `email`, `password`
  - Response: `LoginResponse { user, token }`
- `GET /api/auth/me`
  - Header: `Authorization: Bearer <token>`
  - Response: `UserResponse`
  - 인증 사용자 주입: `@AuthenticatedUser user: User`

### 4.2 Meal APIs

- `GET /api/meals`
  - Query: `date?`, `diningTime?`, `place?`
  - Response: `MealListResponse`
- `GET /api/meals/today`
  - Response: `MealListResponse`
- `GET /api/meals/detail`
  - Query: `date`, `diningTime`, `place`
  - Response: `MealDetailResponse`
- `GET /api/meals/low-calorie`
  - Query: `date?`
  - Response: `MealListResponse`
- `GET /api/meals/vegetarian`
  - Query: `date?`
  - Response: `MealListResponse`

## 5. Error Handling

`GlobalExceptionHandler`에서 도메인/인증/일반 예외를 일관 포맷으로 매핑한다.

- `DomainException`
  - `InvalidCredentials` -> `401`
  - `UserNotFound`, `MealNotFound` -> `404`
  - `DuplicateEmail` -> `409`
  - 그 외 -> `400`
- `UnauthorizedException` -> `401`
- `IllegalArgumentException` -> `400`
- 기타 런타임 예외 -> `500`

## 6. Configuration and Secrets

### 6.1 Profile

- `local` (default)
- `dev`
- `prod`

### 6.2 Secret Injection Policy

환경값은 하드코딩하지 않고 환경변수로 주입한다.

- `KAPP_DB_URL`
- `KAPP_DB_USERNAME`
- `KAPP_DB_PASSWORD`
- `KAPP_JWT_SECRET`
- `KAPP_JWT_ACCESS_TOKEN_EXPIRE_DAYS`

### 6.3 Build-time Config Copy

`api` 모듈은 `kapp-config/kapp-backend` 설정을 `processResources` 시점에 `build/generated-resources/config`로 복사한다.

- task: `copyExternalConfig`
- duplicate 처리: `EXCLUDE`

## 7. Testing Status

### 7.1 Current Tests

- Core
  - `EmailTest`
  - `MoneyTest`
  - `UserDomainServiceTest`
- API
  - `JwtServiceTest`
  - `AuthServiceTest`
  - `GlobalExceptionHandlerTest`
- Infra
  - `PasswordEncoderImplTest`
  - `DBUserRepositoryTest` (H2)
  - `DBMealRepositoryTest` (H2)

### 7.2 Command

```bash
./gradlew clean test
```

## 8. CI/CD

### 8.1 CI

- File: `.github/workflows/ci.yml`
- Trigger: `push(main, develop)`, `pull_request`
- Actions
  - JDK 21 setup
  - Gradle test run

### 8.2 CD

- File: `.github/workflows/cd.yml`
- Trigger: `push(main)`, `workflow_dispatch`
- Actions
  - test verification
  - Docker image build
  - GHCR publish (`ghcr.io/<owner>/k-app-api`)

### 8.3 Container

- File: `Dockerfile`
- Build: `./gradlew :api:bootJar`
- Run: `java -jar /app/app.jar`

## 9. Open Refactoring Backlog

- `api`/`infra` 런타임 결합 해소를 위한 조립 모듈(boot module) 분리
- DB 통합 테스트를 PostgreSQL Testcontainers 기반으로 확장
- 배포 단계(예: dev/prod 환경 배포) 자동화 및 승인 플로우 추가
