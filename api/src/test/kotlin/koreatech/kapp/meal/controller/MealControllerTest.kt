package koreatech.kapp.meal.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import koreatech.kapp.global.PageRequest
import koreatech.kapp.global.PageResponse
import koreatech.kapp.global.SortDirection
import koreatech.kapp.meal.controller.dto.MealResponse
import koreatech.kapp.meal.service.MealService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

/**
 * MealController 통합 테스트
 */
@WebMvcTest(MealController::class)
class MealControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var mealService: MealService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mealService(): MealService = mockk(relaxed = true)
    }

    @Test
    fun `식단 목록 조회 API 테스트 - 페이지네이션`() {
        // given
        val meals = listOf(
            MealResponse(
                id = 1L,
                date = LocalDate.of(2024, 1, 15),
                diningTime = "LUNCH",
                place = "학생식당",
                price = 5000,
                calories = 700,
                menu = listOf("밥", "김치찌개", "샐러드")
            ),
            MealResponse(
                id = 2L,
                date = LocalDate.of(2024, 1, 15),
                diningTime = "DINNER",
                place = "학생식당",
                price = 6000,
                calories = 800,
                menu = listOf("밥", "불고기", "된장국")
            )
        )

        val pageResponse = PageResponse(
            content = meals,
            page = 0,
            size = 20,
            totalElements = 2L,
            totalPages = 1,
            first = true,
            last = true
        )

        every { mealService.getMeals(any(), any()) } returns pageResponse

        // when & then
        mockMvc.perform(
            get("/api/meals")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].place").value("학생식당"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.totalElements").value(2))

        verify(exactly = 1) { mealService.getMeals(any(), any()) }
    }

    @Test
    fun `식단 목록 조회 API 테스트 - 필터링`() {
        // given
        val meals = listOf(
            MealResponse(
                id = 1L,
                date = LocalDate.of(2024, 1, 15),
                diningTime = "LUNCH",
                place = "학생식당",
                price = 5000,
                calories = 700,
                menu = listOf("밥", "김치찌개", "샐러드")
            )
        )

        val pageResponse = PageResponse(
            content = meals,
            page = 0,
            size = 20,
            totalElements = 1L,
            totalPages = 1,
            first = true,
            last = true
        )

        every { mealService.getMeals(any(), any()) } returns pageResponse

        // when & then
        mockMvc.perform(
            get("/api/meals")
                .param("diningTime", "LUNCH")
                .param("minPrice", "4000")
                .param("maxPrice", "6000")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].diningTime").value("LUNCH"))

        verify(exactly = 1) { mealService.getMeals(any(), any()) }
    }

    @Test
    fun `특정 날짜의 식단 조회 API 테스트`() {
        // given
        val date = LocalDate.of(2024, 1, 15)
        val meals = listOf(
            MealResponse(
                id = 1L,
                date = date,
                diningTime = "BREAKFAST",
                place = "학생식당",
                price = 4000,
                calories = 500,
                menu = listOf("밥", "계란후라이", "김")
            ),
            MealResponse(
                id = 2L,
                date = date,
                diningTime = "LUNCH",
                place = "학생식당",
                price = 5000,
                calories = 700,
                menu = listOf("밥", "김치찌개", "샐러드")
            )
        )

        every { mealService.getMealsByDate(date) } returns meals

        // when & then
        mockMvc.perform(
            get("/api/meals/date/2024-01-15")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].date").value("2024-01-15"))
            .andExpect(jsonPath("$[1].date").value("2024-01-15"))

        verify(exactly = 1) { mealService.getMealsByDate(date) }
    }

    @Test
    fun `오늘의 식단 조회 API 테스트`() {
        // given
        val today = LocalDate.now()
        val meals = listOf(
            MealResponse(
                id = 1L,
                date = today,
                diningTime = "LUNCH",
                place = "학생식당",
                price = 5000,
                calories = 700,
                menu = listOf("밥", "김치찌개")
            )
        )

        every { mealService.getMealsByDate(today) } returns meals

        // when & then
        mockMvc.perform(
            get("/api/meals/today")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$[0].diningTime").exists())

        verify(exactly = 1) { mealService.getMealsByDate(today) }
    }

    @Test
    fun `이번 주 식단 조회 API 테스트`() {
        // given
        val pageResponse = PageResponse(
            content = emptyList<MealResponse>(),
            page = 0,
            size = 100,
            totalElements = 0L,
            totalPages = 1,
            first = true,
            last = true
        )

        every { mealService.getMeals(any(), any()) } returns pageResponse

        // when & then
        mockMvc.perform(
            get("/api/meals/this-week")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)

        verify(exactly = 1) { mealService.getMeals(any(), any()) }
    }

    @Test
    fun `식단 목록 조회 API 테스트 - 메뉴 키워드 검색`() {
        // given
        val meals = listOf(
            MealResponse(
                id = 1L,
                date = LocalDate.of(2024, 1, 15),
                diningTime = "LUNCH",
                place = "학생식당",
                price = 5000,
                calories = 700,
                menu = listOf("밥", "김치찌개", "샐러드")
            )
        )

        val pageResponse = PageResponse(
            content = meals,
            page = 0,
            size = 20,
            totalElements = 1L,
            totalPages = 1,
            first = true,
            last = true
        )

        every { mealService.getMeals(any(), any()) } returns pageResponse

        // when & then
        mockMvc.perform(
            get("/api/meals")
                .param("menuKeyword", "김치")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].menu").isArray)

        verify(exactly = 1) { mealService.getMeals(any(), any()) }
    }
}
