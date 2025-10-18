package koreatech.kapp.domain.meal.service

import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.domain.meal.model.Meal
import koreatech.kapp.domain.meal.repository.MealRepository
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 간소화된 식단 도메인 서비스
 * 기본적인 식단 조회 기능만 제공
 */
class MealDomainService(
    private val mealRepository: MealRepository
) {

    /**
     * 특정 날짜의 모든 식단 조회
     */
    fun getMealsByDate(date: LocalDate): List<Meal> {
        return mealRepository.findByDate(date)
    }

    /**
     * 특정 날짜와 식사 시간의 식단 조회
     */
    fun getMealsByDateAndDiningTime(date: LocalDate, diningTime: DiningTime): List<Meal> {
        return mealRepository.findByDate(date)
            .filter { it.diningTime == diningTime }
    }

    /**
     * 특정 날짜의 식단 영양 정보 요약
     */
    fun getMealNutritionSummary(date: LocalDate): MealNutritionSummary {
        val meals = mealRepository.findByDate(date)

        if (meals.isEmpty()) {
            return MealNutritionSummary(
                totalMeals = 0,
                totalCalories = 0,
                averageCaloriesPerMeal = 0,
                averagePrice = java.math.BigDecimal.ZERO,
                breakfastCount = 0,
                lunchCount = 0,
                dinnerCount = 0,
                hasVegetarianOptions = false,
                hasSpicyOptions = false
            )
        }

        val totalCalories = meals.sumOf { it.calories.value }
        val totalPrice =
            meals.fold(java.math.BigDecimal.ZERO) { acc, meal -> acc + meal.price.amount }

        return MealNutritionSummary(
            totalMeals = meals.size,
            totalCalories = totalCalories,
            averageCaloriesPerMeal = totalCalories / meals.size,
            averagePrice = totalPrice.divide(
                java.math.BigDecimal(meals.size),
                0,
                java.math.RoundingMode.HALF_UP
            ),
            breakfastCount = meals.count { it.diningTime == DiningTime.BREAKFAST },
            lunchCount = meals.count { it.diningTime == DiningTime.LUNCH },
            dinnerCount = meals.count { it.diningTime == DiningTime.DINNER },
            hasVegetarianOptions = meals.any { it.menu.hasVegetarianOptions() },
            hasSpicyOptions = meals.any { it.menu.hasSpicyItems() }
        )
    }

    /**
     * 저칼로리 식단 필터링
     */
    fun getLowCalorieMeals(date: LocalDate): List<Meal> {
        return mealRepository.findByDate(date)
            .filter { it.isLowCalorie() }
    }

    /**
     * 채식 옵션이 있는 식단 필터링
     */
    fun getVegetarianMeals(date: LocalDate): List<Meal> {
        return mealRepository.findByDate(date)
            .filter { it.menu.hasVegetarianOptions() }
    }

    /**
     * 특정 장소의 식단 조회
     */
    fun getMealsByPlace(date: LocalDate, place: String): List<Meal> {
        return mealRepository.findByDate(date)
            .filter { it.place.contains(place, ignoreCase = true) }
    }
}

data class MealNutritionSummary(
    val totalMeals: Int,
    val totalCalories: Int,
    val averageCaloriesPerMeal: Int,
    val averagePrice: BigDecimal,
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int,
    val hasVegetarianOptions: Boolean,
    val hasSpicyOptions: Boolean
)
