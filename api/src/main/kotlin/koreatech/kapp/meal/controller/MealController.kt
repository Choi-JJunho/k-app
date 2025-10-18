package koreatech.kapp.meal.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.global.ErrorResponse
import koreatech.kapp.meal.controller.dto.MealDetailResponse
import koreatech.kapp.meal.controller.dto.MealListResponse
import koreatech.kapp.meal.service.MealService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 식단 관련 API 엔드포인트
 * 식단 조회, 필터링, 추천 기능 제공
 */
@RestController
@RequestMapping("/api/meals")
@Tag(name = "Meal API", description = "식단 관리 API")
class MealController(
    private val mealService: MealService
) {

    @Operation(
        summary = "식단 목록 조회",
        description = "날짜, 식사 시간, 장소로 필터링하여 식단 목록을 조회합니다. 파라미터가 없으면 오늘의 모든 식단을 반환합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = MealListResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @GetMapping
    fun getMeals(
        @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식). 기본값: 오늘")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,

        @Parameter(description = "식사 시간 (BREAKFAST, LUNCH, DINNER)")
        @RequestParam(required = false)
        diningTime: DiningTime?,

        @Parameter(description = "식당 장소 (부분 일치 검색)")
        @RequestParam(required = false)
        place: String?
    ): ResponseEntity<MealListResponse> {
        val response = mealService.getMeals(date, diningTime, place)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "오늘의 식단 조회",
        description = "오늘 날짜의 모든 식단을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = MealListResponse::class))]
        )
    )
    @GetMapping("/today")
    fun getTodayMeals(): ResponseEntity<MealListResponse> {
        val response = mealService.getTodayMeals()
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "식단 상세 조회",
        description = "날짜, 식사 시간, 장소를 이용하여 특정 식단의 상세 정보를 조회합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = MealDetailResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "해당 식단을 찾을 수 없음",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )
    )
    @GetMapping("/detail")
    fun getMealDetail(
        @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", required = true)
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate,

        @Parameter(description = "식사 시간 (BREAKFAST, LUNCH, DINNER)", required = true)
        @RequestParam
        diningTime: DiningTime,

        @Parameter(description = "식당 장소", required = true)
        @RequestParam
        place: String
    ): ResponseEntity<MealDetailResponse> {
        val response = mealService.getMealDetail(date, diningTime, place)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "저칼로리 식단 조회",
        description = "지정한 날짜의 저칼로리 식단(500kcal 미만)을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = MealListResponse::class))]
        )
    )
    @GetMapping("/low-calorie")
    fun getLowCalorieMeals(
        @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식). 기본값: 오늘")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): ResponseEntity<MealListResponse> {
        val response = mealService.getLowCalorieMeals(date)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "채식 옵션 식단 조회",
        description = "지정한 날짜의 채식 옵션이 포함된 식단을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = MealListResponse::class))]
        )
    )
    @GetMapping("/vegetarian")
    fun getVegetarianMeals(
        @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식). 기본값: 오늘")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): ResponseEntity<MealListResponse> {
        val response = mealService.getVegetarianMeals(date)
        return ResponseEntity.ok(response)
    }
}
