package koreatech.kapp.meal.controller.dto

import koreatech.kapp.domain.meal.model.Meal
import java.time.LocalDate

/**
 * 식단 관련 DTO 클래스들
 */
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

data class MealListResponse(
    val meals: List<MealResponse>,
    val totalCount: Int
)

data class MealDetailResponse(
    val id: Long,
    val date: String,
    val diningTime: String,
    val diningTimeDisplay: String,
    val place: String,
    val price: String,  // JSON에서는 문자열로 전송
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

// Mapper extensions
fun Meal.toMealResponse(): MealResponse {
    return MealResponse(
        id = id?.value ?: throw IllegalStateException("Meal ID cannot be null"),
        date = date,
        diningTime = diningTime.name,
        place = place,
        price = price.amount.toInt(),
        calories = calories.value,
        menu = menu.items,
        isToday = isToday(),
        isWeekend = isWeekend(),
        isHighPriced = isHighPriced(),
        isLowCalorie = isLowCalorie()
    )
}

fun Meal.toMealDetailResponse(): MealDetailResponse {
    return MealDetailResponse(
        id = id?.value ?: throw IllegalStateException("Meal ID cannot be null"),
        date = date.toString(),
        diningTime = diningTime.name,
        diningTimeDisplay = diningTime.displayName,
        place = place,
        price = price.amount.toString(),
        calories = calories.value,
        menu = menu.items,
        menuSize = menu.size(),
        isToday = isToday(),
        isWeekend = isWeekend(),
        isHighPriced = isHighPriced(),
        isLowCalorie = isLowCalorie(),
        hasVegetarianOptions = menu.hasVegetarianOptions(),
        hasSpicyItems = menu.hasSpicyItems()
    )
}
