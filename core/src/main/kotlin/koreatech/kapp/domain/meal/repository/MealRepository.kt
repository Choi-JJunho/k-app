package koreatech.kapp.domain.meal.repository

import koreatech.kapp.domain.meal.model.Meal
import java.time.LocalDate

/**
 * 식단 도메인 리포지토리 인터페이스
 */
interface MealRepository {
    fun findByDate(date: LocalDate): List<Meal>
}
