package koreatech.kapp.dto

import koreatech.kapp.domain.meal.model.Meal
import java.time.LocalDate

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

// Extension functions for conversion
fun Meal.toDto(): MealDto {
    return MealDto(
        id = id?.value,
        date = date.toString(),
        diningTime = diningTime.name.lowercase(),
        place = place,
        price = price.amount.toString(),
        kcal = calories.value.toString(),
        menu = menu.items
    )
}

fun List<Meal>.toMealListResponse(date: LocalDate): MealListResponse {
    return MealListResponse(
        meals = this.map { it.toDto() },
        totalCount = this.size,
        date = date.toString()
    )
}