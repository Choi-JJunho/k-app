package koreatech.kapp.domain.meal.service

import koreatech.kapp.domain.meal.model.*
import koreatech.kapp.domain.meal.repository.MealRepository
import koreatech.kapp.domain.common.MealNotFound
import java.time.LocalDate

/**
 * 간소화된 식단 도메인 서비스
 * 기본적인 식단 조회 기능만 제공
 */
class MealDomainService(
    private val mealRepository: MealRepository
) {

}

data class MealNutritionSummary(
    val totalMeals: Int,
    val totalCalories: Int,
    val averageCaloriesPerMeal: Int,
    val averagePrice: Int,
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int,
    val hasVegetarianOptions: Boolean,
    val hasSpicyOptions: Boolean
)
