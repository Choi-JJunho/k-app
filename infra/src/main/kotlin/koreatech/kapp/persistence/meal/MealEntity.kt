package koreatech.kapp.persistence.meal

import koreatech.kapp.domain.common.Money
import koreatech.kapp.domain.meal.model.Calories
import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.domain.meal.model.Meal
import koreatech.kapp.domain.meal.model.MealId
import koreatech.kapp.domain.meal.model.Menu
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 식단 JooQ 레코드
 */
data class MealRecord(
    val id: Long? = null,
    val date: LocalDate,
    val diningTime: DiningTime,
    val place: String,
    val price: String,
    val kcal: String,
    val menuItems: List<String>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * JooQ 레코드를 도메인 모델로 변환
     */
    fun toDomain(): Meal {
        return Meal(
            id = id?.let { MealId(it) },
            date = date,
            diningTime = diningTime,
            place = place,
            price = Money.Companion.of(price),
            calories = Calories(kcal.toInt()),
            menu = Menu(menuItems),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
