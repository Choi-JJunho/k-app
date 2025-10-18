package koreatech.kapp.meal.service

import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.domain.meal.service.MealDomainService
import koreatech.kapp.meal.controller.dto.MealDetailResponse
import koreatech.kapp.meal.controller.dto.MealListResponse
import koreatech.kapp.meal.controller.dto.toMealDetailResponse
import koreatech.kapp.meal.controller.dto.toMealResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * 식단 애플리케이션 서비스
 * 도메인 서비스를 조합하여 API 레벨의 비즈니스 로직 처리
 */
@Service
class MealService(
    private val mealDomainService: MealDomainService
) {

    /**
     * 식단 목록 조회 (날짜, 식사시간, 장소로 필터링 가능)
     */
    fun getMeals(
        date: LocalDate?,
        diningTime: DiningTime?,
        place: String?
    ): MealListResponse {
        val targetDate = date ?: LocalDate.now()

        val meals = when {
            diningTime != null -> mealDomainService.getMealsByDateAndDiningTime(targetDate, diningTime)
            place != null -> mealDomainService.getMealsByPlace(targetDate, place)
            else -> mealDomainService.getMealsByDate(targetDate)
        }

        val mealResponses = meals.map { it.toMealResponse() }

        return MealListResponse(
            meals = mealResponses,
            totalCount = mealResponses.size
        )
    }

    /**
     * 특정 식단 상세 조회
     */
    fun getMealDetail(date: LocalDate, diningTime: DiningTime, place: String): MealDetailResponse {
        val meals = mealDomainService.getMealsByDateAndDiningTime(date, diningTime)

        val meal = meals.firstOrNull { it.place.equals(place, ignoreCase = true) }
            ?: throw IllegalArgumentException("해당 조건의 식단을 찾을 수 없습니다")

        return meal.toMealDetailResponse()
    }

    /**
     * 오늘의 식단 조회
     */
    fun getTodayMeals(): MealListResponse {
        return getMeals(date = LocalDate.now(), diningTime = null, place = null)
    }

    /**
     * 저칼로리 식단 추천
     */
    fun getLowCalorieMeals(date: LocalDate?): MealListResponse {
        val targetDate = date ?: LocalDate.now()
        val meals = mealDomainService.getLowCalorieMeals(targetDate)
        val mealResponses = meals.map { it.toMealResponse() }

        return MealListResponse(
            meals = mealResponses,
            totalCount = mealResponses.size
        )
    }

    /**
     * 채식 옵션 식단 조회
     */
    fun getVegetarianMeals(date: LocalDate?): MealListResponse {
        val targetDate = date ?: LocalDate.now()
        val meals = mealDomainService.getVegetarianMeals(targetDate)
        val mealResponses = meals.map { it.toMealResponse() }

        return MealListResponse(
            meals = mealResponses,
            totalCount = mealResponses.size
        )
    }
}
