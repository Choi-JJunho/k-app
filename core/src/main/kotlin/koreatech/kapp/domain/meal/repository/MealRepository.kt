package koreatech.kapp.domain.meal.repository

import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.domain.meal.model.Meal
import koreatech.kapp.domain.meal.model.MealId
import java.time.LocalDate

/**
 * 식단 도메인 리포지토리 인터페이스
 */
interface MealRepository {
    fun save(meal: Meal): Meal
    fun findById(id: MealId): Meal?
    fun findByDate(date: LocalDate): List<Meal>
    fun findByDateAndDiningTime(date: LocalDate, diningTime: DiningTime): List<Meal>
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): List<Meal>
    fun findByPlace(place: String): List<Meal>
    fun delete(meal: Meal)
}
