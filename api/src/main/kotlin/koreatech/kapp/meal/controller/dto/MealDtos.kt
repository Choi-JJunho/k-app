package koreatech.kapp.meal.controller.dto

/**
 * 식단 관련 DTO 클래스들
 */
data class MealDto(
    val id: Long?,
    val date: String,
    val diningTime: String,
    val place: String,
    val price: String,
    val kcal: String,
    val menu: List<String>
)

data class MealListResponse(
    val meals: List<MealDto>,
    val totalCount: Int,
    val date: String
)

data class MealRecommendationResponse(
    val recommendedMeals: List<MealDto>,
    val reason: String,
    val date: String
)
