# K-App Domain Knowledge

## Bounded Context

### User Context
- 목표: 회원 가입, 로그인, 현재 사용자 조회
- Aggregate: `User`
- Value Objects:
  - `Email`
  - `HashedPassword`
- 핵심 규칙:
  - 이메일은 시스템 내에서 유일해야 한다.
  - 비밀번호는 해시 형태로만 저장한다.
  - 사용자 이름과 학번/사번은 공백일 수 없다.

### Meal Context
- 목표: 날짜/식사시간/장소 기준 식단 조회 및 필터링
- Aggregate: `Meal`
- Value Objects:
  - `Money`
  - `Calories`
  - `Menu`
- 핵심 규칙:
  - 식당명은 비어 있을 수 없다.
  - 메뉴는 최소 1개 이상이어야 한다.
  - 칼로리는 0~9999 범위여야 한다.
  - 식단 날짜는 현재 기준 7일 이후를 넘을 수 없다.

## Ubiquitous Language
- DiningTime: `BREAKFAST`, `LUNCH`, `DINNER`
- High priced meal: `6000 KRW` 초과
- Low calorie meal: `500 kcal` 미만
- Vegetarian options: 메뉴 항목 중 채식 키워드를 포함

## Application Use Cases
- 회원가입
- 로그인
- 로그인 사용자 조회
- 오늘 식단 조회
- 조건 기반 식단 조회
- 저칼로리/채식 식단 조회

## Open Decisions (Refactoring Targets)
- 인증 토큰 정책: Access Token 만료/재발급 정책 분리 필요
- Meal 가격 표현: KRW 고정/다중 통화 지원 여부 결정 필요
- 메뉴 분류 규칙: 키워드 기반에서 정책 객체로 확장 필요
