package koreatech.kapp.meal.controller

import koreatech.kapp.domain.meal.model.DiningTime
import java.time.LocalDate

/**
 * 식단 필터링 조건
 */
data class MealFilter(
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val diningTime: DiningTime? = null,
    val place: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minCalories: Int? = null,
    val maxCalories: Int? = null,
    val menuKeyword: String? = null
) {
    fun hasDateRange(): Boolean = startDate != null && endDate != null
    fun hasDiningTime(): Boolean = diningTime != null
    fun hasPlace(): Boolean = !place.isNullOrBlank()
    fun hasPriceRange(): Boolean = minPrice != null || maxPrice != null
    fun hasCaloriesRange(): Boolean = minCalories != null || maxCalories != null
    fun hasMenuKeyword(): Boolean = !menuKeyword.isNullOrBlank()
}
