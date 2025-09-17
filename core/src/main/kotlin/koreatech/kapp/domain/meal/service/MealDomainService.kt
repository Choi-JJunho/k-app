package koreatech.kapp.domain.meal.service

import koreatech.kapp.domain.meal.model.*
import koreatech.kapp.domain.meal.repository.MealRepository
import koreatech.kapp.domain.shared.MealNotFound
import java.time.LocalDate

/**
 * 간소화된 식단 도메인 서비스
 * 기본적인 식단 조회 기능만 제공
 */
class MealDomainService(
    private val mealRepository: MealRepository
) {

    fun getMealsByDate(date: LocalDate): List<Meal> {
        return mealRepository.findByDate(date)
            .sortedWith(compareBy<Meal> { it.diningTime.ordinal }.thenBy { it.place })
    }

    fun getMealsByDateRange(startDate: LocalDate, endDate: LocalDate): List<Meal> {
        return mealRepository.findByDateRange(startDate, endDate)
            .sortedWith(compareBy<Meal> { it.date }.thenBy { it.diningTime.ordinal }.thenBy { it.place })
    }

    fun analyzeMealNutrition(meals: List<Meal>): MealNutritionSummary {
        val totalCalories = meals.sumOf { it.calories.value }
        val averagePrice = meals.map { it.price.amount.toInt() }.average()

        val mealsByTime = meals.groupBy { it.diningTime }

        return MealNutritionSummary(
            totalMeals = meals.size,
            totalCalories = totalCalories,
            averageCaloriesPerMeal = if (meals.isNotEmpty()) totalCalories / meals.size else 0,
            averagePrice = averagePrice.toInt(),
            breakfastCount = mealsByTime[DiningTime.BREAKFAST]?.size ?: 0,
            lunchCount = mealsByTime[DiningTime.LUNCH]?.size ?: 0,
            dinnerCount = mealsByTime[DiningTime.DINNER]?.size ?: 0,
            hasVegetarianOptions = meals.any { it.menu.hasVegetarianOptions() },
            hasSpicyOptions = meals.any { it.menu.hasSpicyItems() }
        )
    }

    fun getMealById(mealId: MealId): Meal {
        return mealRepository.findById(mealId)
            ?: throw MealNotFound(mealId.value.toString())
    }
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
