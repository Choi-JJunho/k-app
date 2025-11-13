package koreatech.kapp.meal.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.global.PageRequest
import koreatech.kapp.global.PageResponse
import koreatech.kapp.global.SortDirection
import koreatech.kapp.meal.controller.dto.MealResponse
import koreatech.kapp.meal.service.MealService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Meal", description = "식단 관리 API")
@RestController
@RequestMapping("/api/meals")
class MealController(
    private val mealService: MealService
) {

    @Operation(summary = "식단 목록 조회 (페이지네이션 및 필터링)")
    @GetMapping
    fun getMeals(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false, defaultValue = "DESC") direction: SortDirection,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) diningTime: DiningTime?,
        @RequestParam(required = false) place: String?,
        @RequestParam(required = false) minPrice: Int?,
        @RequestParam(required = false) maxPrice: Int?,
        @RequestParam(required = false) minCalories: Int?,
        @RequestParam(required = false) maxCalories: Int?,
        @RequestParam(required = false) menuKeyword: String?
    ): ResponseEntity<PageResponse<MealResponse>> {
        val pageRequest = PageRequest(page, size, sort, direction)
        val filter = MealFilter(
            startDate, endDate, diningTime, place,
            minPrice, maxPrice, minCalories, maxCalories, menuKeyword
        )

        val response = mealService.getMeals(pageRequest, filter)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "특정 날짜의 식단 조회")
    @GetMapping("/date/{date}")
    fun getMealsByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<List<MealResponse>> {
        val meals = mealService.getMealsByDate(date)
        return ResponseEntity.ok(meals)
    }

    @Operation(summary = "오늘의 식단 조회")
    @GetMapping("/today")
    fun getTodayMeals(): ResponseEntity<List<MealResponse>> {
        val meals = mealService.getMealsByDate(LocalDate.now())
        return ResponseEntity.ok(meals)
    }

    @Operation(summary = "이번 주 식단 조회")
    @GetMapping("/this-week")
    fun getThisWeekMeals(): ResponseEntity<List<MealResponse>> {
        val today = LocalDate.now()
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)

        val filter = MealFilter(startDate = startOfWeek, endDate = endOfWeek)
        val pageRequest = PageRequest(page = 0, size = 100)

        val response = mealService.getMeals(pageRequest, filter)
        return ResponseEntity.ok(response.content)
    }
}
