# Meal API Implementation (Current)

## 1. Summary

- Status: Implemented
- Layer split
  - Controller: `api/.../meal/controller`
  - Application Service: `api/.../meal/service/MealService.kt`
  - Domain Service: `core/.../meal/service/MealDomainService.kt`
  - Repository Port: `core/.../meal/repository/MealRepository.kt`
  - Repository Adapter: `infra/.../meal/DBMealRepository.kt`

## 2. Endpoints

- `GET /api/meals`
  - Optional query: `date`, `diningTime`, `place`
- `GET /api/meals/today`
- `GET /api/meals/detail`
  - Required query: `date`, `diningTime`, `place`
- `GET /api/meals/low-calorie`
  - Optional query: `date`
- `GET /api/meals/vegetarian`
  - Optional query: `date`

## 3. DTO Shape

### 3.1 MealResponse

```kotlin
data class MealResponse(
    val id: Long,
    val date: LocalDate,
    val diningTime: String,
    val place: String,
    val price: Int,
    val calories: Int,
    val menu: List<String>,
    val isToday: Boolean,
    val isWeekend: Boolean,
    val isHighPriced: Boolean,
    val isLowCalorie: Boolean
)
```

### 3.2 MealDetailResponse

```kotlin
data class MealDetailResponse(
    val id: Long,
    val date: String,
    val diningTime: String,
    val diningTimeDisplay: String,
    val place: String,
    val price: String,
    val calories: Int,
    val menu: List<String>,
    val menuSize: Int,
    val isToday: Boolean,
    val isWeekend: Boolean,
    val isHighPriced: Boolean,
    val isLowCalorie: Boolean,
    val hasVegetarianOptions: Boolean,
    val hasSpicyItems: Boolean
)
```

## 4. Domain Rules Used

- `Meal.isToday()`
- `Meal.isWeekend()`
- `Meal.isHighPriced()` (6000원 초과)
- `Meal.isLowCalorie()` (500kcal 미만)
- `Menu.hasVegetarianOptions()`
- `Menu.hasSpicyItems()`

## 5. Error Behavior

- 상세 조회에서 대상 식단이 없으면 `IllegalArgumentException` 발생
- 전역 예외 처리기에서 `400 BAD_REQUEST`로 응답

## 6. Test Coverage (Meal-related)

- `DBMealRepositoryTest` (infra)
  - 날짜 기준 조회
  - 메뉴 아이템 매핑

추가 권장:
- `MealService` 필터 조합 시나리오 단위 테스트
- `MealController` 웹 계층 테스트(MockMvc)
