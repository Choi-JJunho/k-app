# K-Food API 명세서

K-Food 프로젝트의 REST API 명세서입니다.

## 기본 정보

- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`
- **인코딩**: UTF-8

---

## 1. 식단 조회 API

### 1.1 일별 식단 조회

특정 날짜의 전체 식단 메뉴를 조회합니다.

**Endpoint**

```
GET /api/meals?date={date}
```

**Parameters**
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| date | string | 필수 | 조회할 날짜 (YYYY-MM-DD 형식) | 2025-01-01 |

**Request Example**

```bash
GET /api/meals?date=2025-01-01
```

**Response Example (Success - 200)**

```json
[
  {
    "date": "2025-01-01",
    "dining_time": "lunch",
    "place": "Korean Food (한식)",
    "price": "5000",
    "kcal": "885",
    "menu": [
      "혼합잡곡밥＆흰밥",
      "(탕)사골왕만두떡국",
      "제육김치볶음",
      "매콤두부조림",
      "깍두기"
    ]
  },
  {
    "date": "2025-01-01",
    "dining_time": "dinner",
    "place": "Korean Food (한식)",
    "price": "5000",
    "kcal": "892",
    "menu": [
      "혼합잡곡밥＆흰밥",
      "부대찌개",
      "돈육떡불고기",
      "시금치나물",
      "김치"
    ]
  },
  {
    "date": "2025-01-01",
    "dining_time": "lunch",
    "place": "Onedish Food (일품)",
    "price": "5000",
    "kcal": "890",
    "menu": [
      "치킨마요덮밥",
      "미소시루",
      "단무지"
    ]
  }
]
```

**Response Example (Error - 400)**

```json
{
  "error": "날짜를 YYYY-MM-DD 형식으로 입력해주세요."
}
```

**Response Example (Error - 500)**

```json
{
  "error": "데이터를 가져오는 중 서버에서 오류가 발생했습니다."
}
```

---

## 데이터 모델

### Meal Object

식단 정보를 나타내는 객체입니다.

```typescript
interface Meal {
  date: string;        // 날짜 (YYYY-MM-DD)
  dining_time: string; // 식사 시간 (breakfast, lunch, dinner)
  place: string;       // 식당명
  price: string;       // 가격 (원 단위)
  kcal: string;        // 칼로리
  menu: string[];      // 메뉴 목록
}
```

**필드 설명**

| 필드          | 타입       | 설명          | 가능한 값                                                           |
|-------------|----------|-------------|-----------------------------------------------------------------|
| date        | string   | 식단 제공 날짜    | YYYY-MM-DD 형식                                                   |
| dining_time | string   | 식사 시간대      | "breakfast", "lunch", "dinner"                                  |
| place       | string   | 식당/코너명      | "Korean Food (한식)", "Onedish Food (일품)", "Special Menu (특식)", 등 |
| price       | string   | 메뉴 가격       | "5000", "6000" 등 (원 단위)                                         |
| kcal        | string   | 칼로리 정보      | "885", "892" 등                                                  |
| menu        | string[] | 제공되는 메뉴 리스트 | ["혼합잡곡밥＆흰밥", "부대찌개", ...]                                       |

---

## 응답 코드

| 상태 코드 | 설명                  |
|-------|---------------------|
| 200   | 성공                  |
| 400   | 잘못된 요청 (날짜 형식 오류 등) |
| 500   | 서버 내부 오류            |

---

## 확장 API

### 2. 사용자 인증 API

#### 2.1 로그인

**Endpoint**

```
POST /api/auth/login
```

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (Success - 200)**

```json
{
  "user": {
    "id": "user_123",
    "email": "user@example.com",
    "name": "홍길동",
    "avatar": "https://example.com/avatar.jpg"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here"
}
```

**Response (Error - 401)**

```json
{
  "error": "이메일 또는 비밀번호가 올바르지 않습니다."
}
```

#### 2.2 로그아웃

**Endpoint**

```
POST /api/auth/logout
```

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "message": "로그아웃되었습니다."
}
```

#### 2.3 회원가입

**Endpoint**

```
POST /api/auth/register
```

**Request Body**

```json
{
  "email": "newuser@example.com",
  "password": "password123",
  "name": "김철수",
  "confirmPassword": "password123"
}
```

**Response (Success - 201)**

```json
{
  "user": {
    "id": "user_124",
    "email": "newuser@example.com",
    "name": "김철수",
    "avatar": null
  },
  "message": "회원가입이 완료되었습니다."
}
```

**Response (Error - 409)**

```json
{
  "error": "이미 사용 중인 이메일입니다."
}
```

#### 2.4 사용자 정보 조회

**Endpoint**

```
GET /api/auth/me
```

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "id": "user_123",
  "email": "user@example.com",
  "name": "홍길동",
  "avatar": "https://example.com/avatar.jpg",
  "createdAt": "2025-01-01T00:00:00Z",
  "preferences": {
    "allergens": ["견과류", "갑각류"],
    "dietType": "일반식",
    "favoriteRestaurants": ["Korean Food (한식)"]
  }
}
```

#### 2.5 토큰 갱신

**Endpoint**

```
POST /api/auth/refresh
```

**Request Body**

```json
{
  "refreshToken": "refresh_token_here"
}
```

**Response (Success - 200)**

```json
{
  "token": "new_jwt_token_here",
  "refreshToken": "new_refresh_token_here"
}
```

### 3. 영양 정보 API

#### 3.1 일별 영양 정보 조회

**Endpoint**

```
GET /api/nutrition?date={date}
```

**Parameters**
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| date | string | 필수 | 조회할 날짜 (YYYY-MM-DD) | 2025-01-01 |

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "date": "2025-01-01",
  "totalNutrition": {
    "calories": 1850,
    "carbs": 280.5,
    "protein": 85.2,
    "fat": 45.8,
    "fiber": 15.3,
    "sodium": 2100
  },
  "meals": [
    {
      "dining_time": "breakfast",
      "nutrition": {
        "calories": 450,
        "carbs": 65.2,
        "protein": 18.5,
        "fat": 12.3,
        "fiber": 4.1,
        "sodium": 580
      },
      "menus": ["혼합잡곡밥＆흰밥", "미역국", "계란찜"]
    },
    {
      "dining_time": "lunch",
      "nutrition": {
        "calories": 885,
        "carbs": 145.8,
        "protein": 42.7,
        "fat": 22.5,
        "fiber": 8.2,
        "sodium": 1200
      },
      "menus": ["혼합잡곡밥＆흰밥", "제육김치볶음", "매콤두부조림"]
    }
  ],
  "recommendations": {
    "dailyGoal": {
      "calories": 2000,
      "carbs": 300,
      "protein": 80,
      "fat": 60,
      "fiber": 25,
      "sodium": 2300
    },
    "remaining": {
      "calories": 150,
      "carbs": 19.5,
      "protein": -5.2,
      "fat": 14.2,
      "fiber": 9.7,
      "sodium": 200
    }
  }
}
```

#### 3.2 기간별 영양 정보 요약

**Endpoint**

```
GET /api/nutrition/summary?startDate={startDate}&endDate={endDate}&period={period}
```

**Parameters**
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| startDate | string | 필수 | 시작 날짜 | 2025-01-01 |
| endDate | string | 필수 | 종료 날짜 | 2025-01-07 |
| period | string | 선택 | 집계 기간 (daily, weekly, monthly) | weekly |

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "period": "weekly",
  "startDate": "2025-01-01",
  "endDate": "2025-01-07",
  "averageDaily": {
    "calories": 1875,
    "carbs": 285.3,
    "protein": 82.1,
    "fat": 48.2,
    "fiber": 16.8,
    "sodium": 2050
  },
  "totalWeekly": {
    "calories": 13125,
    "carbs": 1997.1,
    "protein": 574.7,
    "fat": 337.4,
    "fiber": 117.6,
    "sodium": 14350
  },
  "trends": {
    "calories": {
      "trend": "increasing",
      "change": "+5.2%"
    },
    "protein": {
      "trend": "stable",
      "change": "+1.1%"
    }
  },
  "insights": [
    "이번 주 평균 칼로리 섭취가 목표보다 6.2% 낮습니다.",
    "단백질 섭취량이 권장량을 초과했습니다.",
    "나트륨 섭취를 줄이는 것이 좋겠습니다."
  ]
}
```

#### 3.3 영양소별 상세 분석

**Endpoint**

```
GET /api/nutrition/analysis?nutrient={nutrient}&period={period}
```

**Parameters**
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| nutrient | string | 필수 | 영양소 타입 | calories, protein, carbs |
| period | string | 선택 | 분석 기간 (7days, 30days, 90days) | 30days |

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "nutrient": "protein",
  "period": "30days",
  "data": [
    {
      "date": "2025-01-01",
      "value": 85.2,
      "goal": 80,
      "status": "above"
    }
  ],
  "statistics": {
    "average": 82.5,
    "min": 65.2,
    "max": 95.8,
    "goal": 80,
    "daysAboveGoal": 18,
    "daysBelowGoal": 12
  }
}
```

### 4. 프로필 관리 API

#### 4.1 프로필 조회

**Endpoint**

```
GET /api/profile
```

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "id": "user_123",
  "email": "user@example.com",
  "name": "홍길동",
  "avatar": "https://example.com/avatar.jpg",
  "phone": "010-1234-5678",
  "birthDate": "1990-05-15",
  "gender": "male",
  "height": 175,
  "weight": 70,
  "activityLevel": "moderate",
  "preferences": {
    "allergens": ["견과류", "갑각류"],
    "dietType": "일반식",
    "favoriteRestaurants": ["Korean Food (한식)", "Onedish Food (일품)"],
    "dislikedFoods": ["브로콜리", "시금치"],
    "spicyLevel": "medium"
  },
  "nutritionGoals": {
    "calories": 2000,
    "carbs": 300,
    "protein": 80,
    "fat": 60,
    "fiber": 25,
    "sodium": 2300
  },
  "createdAt": "2024-12-01T00:00:00Z",
  "updatedAt": "2025-01-10T12:30:00Z"
}
```

#### 4.2 프로필 수정

**Endpoint**

```
PUT /api/profile
```

**Headers**

```
Authorization: Bearer {token}
```

**Request Body**

```json
{
  "name": "홍길동",
  "phone": "010-1234-5678",
  "birthDate": "1990-05-15",
  "gender": "male",
  "height": 175,
  "weight": 70,
  "activityLevel": "moderate"
}
```

**Response (Success - 200)**

```json
{
  "message": "프로필이 성공적으로 업데이트되었습니다.",
  "user": {
    "id": "user_123",
    "name": "홍길동",
    "updatedAt": "2025-01-10T12:35:00Z"
  }
}
```

#### 4.3 식단 선호도 설정

**Endpoint**

```
POST /api/profile/preferences
```

**Headers**

```
Authorization: Bearer {token}
```

**Request Body**

```json
{
  "allergens": ["견과류", "갑각류", "유제품"],
  "dietType": "저염식",
  "favoriteRestaurants": ["Korean Food (한식)"],
  "dislikedFoods": ["브로콜리", "시금치", "가지"],
  "spicyLevel": "mild"
}
```

**Response (Success - 200)**

```json
{
  "message": "선호도 설정이 완료되었습니다.",
  "preferences": {
    "allergens": ["견과류", "갑각류", "유제품"],
    "dietType": "저염식",
    "favoriteRestaurants": ["Korean Food (한식)"],
    "dislikedFoods": ["브로콜리", "시금치", "가지"],
    "spicyLevel": "mild"
  }
}
```

#### 4.4 영양 목표 설정

**Endpoint**

```
PUT /api/profile/nutrition-goals
```

**Headers**

```
Authorization: Bearer {token}
```

**Request Body**

```json
{
  "calories": 1800,
  "carbs": 270,
  "protein": 75,
  "fat": 55,
  "fiber": 20,
  "sodium": 2000
}
```

**Response (Success - 200)**

```json
{
  "message": "영양 목표가 설정되었습니다.",
  "nutritionGoals": {
    "calories": 1800,
    "carbs": 270,
    "protein": 75,
    "fat": 55,
    "fiber": 20,
    "sodium": 2000
  }
}
```

#### 4.5 프로필 이미지 업로드

**Endpoint**

```
POST /api/profile/avatar
```

**Headers**

```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request Body (Form Data)**

```
avatar: [이미지 파일]
```

**Response (Success - 200)**

```json
{
  "message": "프로필 이미지가 업로드되었습니다.",
  "avatar": "https://example.com/avatars/user_123_new.jpg"
}
```

#### 4.6 계정 삭제

**Endpoint**

```
DELETE /api/profile
```

**Headers**

```
Authorization: Bearer {token}
```

**Request Body**

```json
{
  "password": "user_password",
  "confirmDeletion": true
}
```

**Response (Success - 200)**

```json
{
  "message": "계정이 성공적으로 삭제되었습니다."
}
```

### 5. 즐겨찾기 API

#### 5.1 즐겨찾기 목록 조회

**Endpoint**

```
GET /api/favorites
```

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "favorites": [
    {
      "id": "fav_123",
      "mealId": "meal_456",
      "name": "안동찜닭",
      "place": "Korean Food (한식)",
      "price": "5000",
      "kcal": "881",
      "rating": 5,
      "addedAt": "2025-01-03T10:30:00Z",
      "frequency": 12,
      "lastEaten": "2025-01-09T12:00:00Z"
    }
  ],
  "totalCount": 15,
  "statistics": {
    "mostFrequent": {
      "name": "안동찜닭",
      "count": 12
    },
    "highestRated": {
      "name": "치킨마요덮밥",
      "rating": 5
    }
  }
}
```

#### 5.2 즐겨찾기 추가

**Endpoint**

```
POST /api/favorites
```

**Headers**

```
Authorization: Bearer {token}
```

**Request Body**

```json
{
  "mealId": "meal_789",
  "rating": 4
}
```

**Response (Success - 201)**

```json
{
  "message": "즐겨찾기에 추가되었습니다.",
  "favorite": {
    "id": "fav_124",
    "mealId": "meal_789",
    "rating": 4,
    "addedAt": "2025-01-10T14:30:00Z"
  }
}
```

#### 5.3 즐겨찾기 제거

**Endpoint**

```
DELETE /api/favorites/{favoriteId}
```

**Headers**

```
Authorization: Bearer {token}
```

**Response (Success - 200)**

```json
{
  "message": "즐겨찾기에서 제거되었습니다."
}
```

---

## 에러 처리

모든 API는 일관된 에러 응답 형식을 사용합니다:

```json
{
  "error": "에러 메시지 내용"
}
```

**일반적인 에러 메시지**

- `"날짜를 YYYY-MM-DD 형식으로 입력해주세요."` - 날짜 형식 오류
- `"데이터를 가져오는 중 서버에서 오류가 발생했습니다."` - 서버 내부 오류
- `"요청한 날짜의 식단 정보를 찾을 수 없습니다."` - 데이터 없음

---

## 사용 예시

### JavaScript/TypeScript 클라이언트

```javascript
// 식단 조회 함수
async function fetchMeals(date) {
  try {
    const response = await fetch(`/api/meals?date=${date}`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const meals = await response.json();
    
    if (meals.error) {
      throw new Error(meals.error);
    }
    
    return meals;
  } catch (error) {
    console.error('식단 조회 실패:', error.message);
    throw error;
  }
}

// 사용 예시
fetchMeals('2025-01-01')
  .then(meals => {
    console.log('식단 정보:', meals);
    // 점심 메뉴만 필터링
    const lunchMeals = meals.filter(meal => meal.dining_time === 'lunch');
    console.log('점심 메뉴:', lunchMeals);
  })
  .catch(error => {
    console.error('에러 발생:', error);
  });
```

### React Hook 사용 예시

```javascript
// useMeals 훅 사용
import { useMeals } from '@/hooks/useMeals';

function MealComponent() {
  const [selectedDate, setSelectedDate] = useState('2025-01-01');
  const { meals, loading, error, refetch } = useMeals(selectedDate);

  if (loading) return <div>로딩 중...</div>;
  if (error) return <div>에러: {error}</div>;

  return (
    <div>
      <h1>식단 정보</h1>
      {meals.map((meal, index) => (
        <div key={index}>
          <h2>{meal.place} - {meal.dining_time}</h2>
          <p>가격: {meal.price}원 | 칼로리: {meal.kcal}kcal</p>
          <ul>
            {meal.menu.map((item, i) => (
              <li key={i}>{item}</li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  );
}
```

---

## 개발 환경 설정

### 로컬 서버 실행

```bash
npm run dev
# 또는
yarn dev
```

서버는 `http://localhost:8080`에서 실행됩니다.

### API 테스트

```bash
# curl을 사용한 API 테스트
curl "http://localhost:8080/api/meals?date=2025-01-01"
```

---

---

## 인증 및 권한

### JWT 토큰 사용

모든 인증이 필요한 API는 HTTP Header에 JWT 토큰을 포함해야 합니다:

```
Authorization: Bearer {your_jwt_token}
```

### 토큰 만료 처리

토큰이 만료되면 `401 Unauthorized` 응답과 함께 다음 에러가 반환됩니다:

```json
{
  "error": "토큰이 만료되었습니다.",
  "code": "TOKEN_EXPIRED"
}
```

이 경우 `/api/auth/refresh` 엔드포인트를 사용하여 토큰을 갱신하세요.

---

## 데이터 검증 및 제약사항

### 사용자 입력 검증

- **이메일**: 유효한 이메일 형식
- **비밀번호**: 최소 8자, 대소문자, 숫자 포함
- **날짜**: YYYY-MM-DD 형식
- **전화번호**: 010-XXXX-XXXX 형식
- **신장**: 100-250cm 범위
- **체중**: 30-200kg 범위

### 파일 업로드 제약사항

- **프로필 이미지**: 최대 5MB, JPG/PNG 형식
- **지원 형식**: image/jpeg, image/png
- **최대 해상도**: 2048x2048

---

## 페이지네이션

목록 조회 API는 페이지네이션을 지원합니다:

**Query Parameters**

```
?page=1&limit=20&sort=created_at&order=desc
```

**Response Format**

```json
{
  "data": [...],
  "pagination": {
    "currentPage": 1,
    "totalPages": 5,
    "totalItems": 98,
    "limit": 20,
    "hasNext": true,
    "hasPrev": false
  }
}
```

---

## 에러 코드 정의

| 에러 코드              | HTTP 상태 | 설명                |
|--------------------|---------|-------------------|
| `INVALID_REQUEST`  | 400     | 잘못된 요청 형식         |
| `UNAUTHORIZED`     | 401     | 인증 실패             |
| `TOKEN_EXPIRED`    | 401     | 토큰 만료             |
| `FORBIDDEN`        | 403     | 권한 부족             |
| `NOT_FOUND`        | 404     | 리소스를 찾을 수 없음      |
| `CONFLICT`         | 409     | 데이터 충돌 (중복 이메일 등) |
| `VALIDATION_ERROR` | 422     | 입력 데이터 검증 실패      |
| `INTERNAL_ERROR`   | 500     | 서버 내부 오류          |

---

**업데이트**: 2025-01-10  
**버전**: 2.0.0
