package koreatech.kapp.domain.meal.model

import koreatech.kapp.domain.common.Money
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 식단 집계 루트 (Aggregate Root)
 * 식단에 관련된 모든 비즈니스 규칙과 불변식을 관리
 */
data class Meal(
    val id: MealId?,
    val date: LocalDate,
    val diningTime: DiningTime,
    val place: String,
    val price: Money,
    val calories: Calories,
    val menu: Menu,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(place.isNotBlank()) { "식당 이름은 비어있을 수 없습니다." }
        require(!date.isAfter(LocalDate.now().plusDays(7))) { "식단은 일주일 이후까지만 등록할 수 있습니다." }
    }

    fun isToday(): Boolean = date == LocalDate.now()

    fun isWeekend(): Boolean {
        val dayOfWeek = date.dayOfWeek
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY
    }

    fun isHighPriced(): Boolean = price.isGreaterThan(Money.of(6000))

    fun isLowCalorie(): Boolean = calories.value < 500

    fun canBeFavorited(): Boolean = true // 모든 식단은 즐겨찾기 가능

    companion object {
        fun create(
            date: LocalDate,
            diningTime: DiningTime,
            place: String,
            price: Money,
            calories: Calories,
            menu: Menu
        ): Meal {
            return Meal(
                id = null,
                date = date,
                diningTime = diningTime,
                place = place,
                price = price,
                calories = calories,
                menu = menu
            )
        }
    }
}

data class MealId(val value: Long) {
    init {
        require(value > 0) { "식단 ID는 0보다 커야 합니다." }
    }
}

enum class DiningTime(val displayName: String) {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁");

    companion object {
        fun from(value: String): DiningTime = when (value.lowercase()) {
            "breakfast" -> BREAKFAST
            "lunch" -> LUNCH
            "dinner" -> DINNER
            else -> throw IllegalArgumentException("유효하지 않은 식사 시간: $value")
        }
    }
}
