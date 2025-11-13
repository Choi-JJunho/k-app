package koreatech.kapp.meal.controller.dto

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

data class MealResponse(
    val id: Long?,
    val date: LocalDate,
    val diningTime: String,
    val place: String,
    val price: Int,
    val calories: Int,
    val menu: List<String>
) {
    companion object {
        fun from(meal: Meal): MealResponse {
            return MealResponse(
                id = meal.id?.value,
                date = meal.date,
                diningTime = meal.diningTime.name,
                place = meal.place,
                price = meal.price.amount.toInt(),
                calories = meal.calories.value,
                menu = meal.menu.items
            )
        }
    }
}

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
