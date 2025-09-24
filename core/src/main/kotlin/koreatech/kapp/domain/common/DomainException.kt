package koreatech.kapp.domain.common

/**
 * 도메인 레벨 예외 클래스
 */
sealed class DomainException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)

class UserNotFound(userId: String) : DomainException("사용자를 찾을 수 없습니다: $userId")

class InvalidCredentials : DomainException("이메일 또는 비밀번호가 올바르지 않습니다")

class DuplicateEmail(email: String) : DomainException("이미 사용 중인 이메일입니다: $email")

class MealNotFound(mealId: String) : DomainException("식단을 찾을 수 없습니다: $mealId")

class InvalidMealData(reason: String) : DomainException("유효하지 않은 식단 데이터: $reason")

class InvalidNutritionGoals(reason: String) : DomainException("유효하지 않은 영양 목표: $reason")

class FavoriteNotFound(favoriteId: String) : DomainException("즐겨찾기를 찾을 수 없습니다: $favoriteId")

class InvalidUserProfile(reason: String) : DomainException("유효하지 않은 사용자 프로필: $reason")
