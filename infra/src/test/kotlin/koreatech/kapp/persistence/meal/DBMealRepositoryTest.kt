package koreatech.kapp.persistence.meal

import koreatech.kapp.domain.common.Money
import koreatech.kapp.domain.meal.model.Calories
import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.domain.meal.model.Meal
import koreatech.kapp.domain.meal.model.Menu
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * DBMealRepository 통합 테스트
 * 실제 데이터베이스(H2)와 JooQ를 사용한 통합 테스트
 */
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DBMealRepositoryTest {

    @Autowired
    private lateinit var dsl: DSLContext

    private lateinit var repository: DBMealRepository

    @AfterEach
    fun cleanup() {
        // 각 테스트 후 데이터 정리
        dsl.deleteFrom(MealMenuItemTable).execute()
        dsl.deleteFrom(MealTable).execute()
    }

    private fun setupRepository() {
        repository = DBMealRepository(dsl)
    }

    @Test
    fun `특정 날짜의 식단을 조회할 수 있다`() {
        // given
        setupRepository()
        val date = LocalDate.of(2024, 1, 15)

        // 식단 데이터 직접 삽입
        val mealId = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date)
            .set(MealTable.DINING_TIME, "LUNCH")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "5000")
            .set(MealTable.KCAL, "700")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        // 메뉴 아이템 삽입
        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, mealId)
            .set(MealMenuItemTable.MENU_ITEM, "밥")
            .execute()

        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, mealId)
            .set(MealMenuItemTable.MENU_ITEM, "김치찌개")
            .execute()

        // when
        val meals = repository.findByDate(date)

        // then
        assertEquals(1, meals.size)
        val meal = meals[0]
        assertEquals(date, meal.date)
        assertEquals(DiningTime.LUNCH, meal.diningTime)
        assertEquals("학생식당", meal.place)
        assertEquals(2, meal.menu.size())
        assertTrue(meal.menu.contains("밥"))
        assertTrue(meal.menu.contains("김치찌개"))
    }

    @Test
    fun `특정 날짜에 식단이 없으면 빈 리스트를 반환한다`() {
        // given
        setupRepository()
        val date = LocalDate.of(2024, 1, 15)

        // when
        val meals = repository.findByDate(date)

        // then
        assertTrue(meals.isEmpty())
    }

    @Test
    fun `같은 날짜에 여러 식사 시간의 식단을 조회할 수 있다`() {
        // given
        setupRepository()
        val date = LocalDate.of(2024, 1, 15)

        // 아침 식단
        val breakfastId = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date)
            .set(MealTable.DINING_TIME, "BREAKFAST")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "4000")
            .set(MealTable.KCAL, "500")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, breakfastId)
            .set(MealMenuItemTable.MENU_ITEM, "토스트")
            .execute()

        // 점심 식단
        val lunchId = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date)
            .set(MealTable.DINING_TIME, "LUNCH")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "5000")
            .set(MealTable.KCAL, "700")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, lunchId)
            .set(MealMenuItemTable.MENU_ITEM, "김치찌개")
            .execute()

        // 저녁 식단
        val dinnerId = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date)
            .set(MealTable.DINING_TIME, "DINNER")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "6000")
            .set(MealTable.KCAL, "800")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, dinnerId)
            .set(MealMenuItemTable.MENU_ITEM, "불고기")
            .execute()

        // when
        val meals = repository.findByDate(date)

        // then
        assertEquals(3, meals.size)

        val diningTimes = meals.map { it.diningTime }.toSet()
        assertTrue(diningTimes.contains(DiningTime.BREAKFAST))
        assertTrue(diningTimes.contains(DiningTime.LUNCH))
        assertTrue(diningTimes.contains(DiningTime.DINNER))
    }

    @Test
    fun `메뉴가 여러 개인 식단을 조회할 수 있다`() {
        // given
        setupRepository()
        val date = LocalDate.of(2024, 1, 15)

        val mealId = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date)
            .set(MealTable.DINING_TIME, "LUNCH")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "5000")
            .set(MealTable.KCAL, "700")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        // 여러 메뉴 아이템 삽입
        val menuItems = listOf("밥", "김치찌개", "샐러드", "과일", "물")
        menuItems.forEach { item ->
            dsl.insertInto(MealMenuItemTable)
                .set(MealMenuItemTable.MEAL_ID, mealId)
                .set(MealMenuItemTable.MENU_ITEM, item)
                .execute()
        }

        // when
        val meals = repository.findByDate(date)

        // then
        assertEquals(1, meals.size)
        val meal = meals[0]
        assertEquals(5, meal.menu.size())
        menuItems.forEach { item ->
            assertTrue(meal.menu.contains(item))
        }
    }

    @Test
    fun `다른 날짜의 식단은 조회되지 않는다`() {
        // given
        setupRepository()
        val date1 = LocalDate.of(2024, 1, 15)
        val date2 = LocalDate.of(2024, 1, 16)

        // date1의 식단
        val mealId1 = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date1)
            .set(MealTable.DINING_TIME, "LUNCH")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "5000")
            .set(MealTable.KCAL, "700")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, mealId1)
            .set(MealMenuItemTable.MENU_ITEM, "김치찌개")
            .execute()

        // date2의 식단
        val mealId2 = dsl.insertInto(MealTable)
            .set(MealTable.DATE, date2)
            .set(MealTable.DINING_TIME, "LUNCH")
            .set(MealTable.PLACE, "학생식당")
            .set(MealTable.PRICE, "5000")
            .set(MealTable.KCAL, "700")
            .returningResult(MealTable.ID)
            .fetchOne()?.value1()

        dsl.insertInto(MealMenuItemTable)
            .set(MealMenuItemTable.MEAL_ID, mealId2)
            .set(MealMenuItemTable.MENU_ITEM, "된장찌개")
            .execute()

        // when
        val mealsDate1 = repository.findByDate(date1)
        val mealsDate2 = repository.findByDate(date2)

        // then
        assertEquals(1, mealsDate1.size)
        assertEquals(1, mealsDate2.size)
        assertTrue(mealsDate1[0].menu.contains("김치찌개"))
        assertTrue(mealsDate2[0].menu.contains("된장찌개"))
    }
}
