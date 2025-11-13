package koreatech.kapp.meal.service

import koreatech.kapp.domain.meal.repository.MealRepository
import koreatech.kapp.global.PageRequest
import koreatech.kapp.global.PageResponse
import koreatech.kapp.meal.controller.MealFilter
import koreatech.kapp.meal.controller.dto.MealResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class MealService(
    private val mealRepository: MealRepository
) {

    fun getMeals(pageRequest: PageRequest, filter: MealFilter): PageResponse<MealResponse> {
        // 날짜 범위에 따른 식단 조회
        val allMeals = if (filter.hasDateRange()) {
            val startDate = filter.startDate ?: LocalDate.now()
            val endDate = filter.endDate ?: LocalDate.now()

            // 날짜 범위 내의 모든 식단을 조회
            val meals = mutableListOf<koreatech.kapp.domain.meal.model.Meal>()
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                meals.addAll(mealRepository.findByDate(currentDate))
                currentDate = currentDate.plusDays(1)
            }
            meals
        } else {
            // 기본적으로 오늘 날짜의 식단을 반환
            mealRepository.findByDate(LocalDate.now())
        }

        // 필터링 적용
        val filteredMeals = allMeals
            .filter { meal ->
                (filter.diningTime == null || meal.diningTime == filter.diningTime) &&
                (filter.place.isNullOrBlank() || meal.place.contains(filter.place, ignoreCase = true)) &&
                (filter.minPrice == null || meal.price.amount.toInt() >= filter.minPrice) &&
                (filter.maxPrice == null || meal.price.amount.toInt() <= filter.maxPrice) &&
                (filter.minCalories == null || meal.calories.value >= filter.minCalories) &&
                (filter.maxCalories == null || meal.calories.value <= filter.maxCalories) &&
                (filter.menuKeyword.isNullOrBlank() ||
                    meal.menu.items.any { it.contains(filter.menuKeyword, ignoreCase = true) })
            }

        // 페이지네이션 적용
        val totalElements = filteredMeals.size.toLong()
        val paginatedMeals = filteredMeals
            .drop(pageRequest.offset())
            .take(pageRequest.size)

        val responses = paginatedMeals.map { MealResponse.from(it) }

        return PageResponse.of(responses, pageRequest, totalElements)
    }

    fun getMealsByDate(date: LocalDate): List<MealResponse> {
        val meals = mealRepository.findByDate(date)
        return meals.map { MealResponse.from(it) }
    }
}
