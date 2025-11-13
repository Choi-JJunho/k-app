package koreatech.kapp.domain.meal.model

import koreatech.kapp.domain.common.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MealTest {

    @Test
    fun `create 팩토리 메서드로 새로운 식단을 생성할 수 있다`() {
        // given
        val date = LocalDate.now()
        val diningTime = DiningTime.LUNCH
        val place = "학생식당"
        val price = Money.of(5000)
        val calories = Calories(700)
        val menu = Menu(listOf("밥", "김치찌개", "샐러드"))

        // when
        val meal = Meal.create(
            date = date,
            diningTime = diningTime,
            place = place,
            price = price,
            calories = calories,
            menu = menu
        )

        // then
        assertNull(meal.id)
        assertEquals(date, meal.date)
        assertEquals(diningTime, meal.diningTime)
        assertEquals(place, meal.place)
        assertEquals(price, meal.price)
        assertEquals(calories, meal.calories)
        assertEquals(menu, meal.menu)
    }

    @Test
    fun `식당 이름이 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Meal.create(
                date = LocalDate.now(),
                diningTime = DiningTime.LUNCH,
                place = "",
                price = Money.of(5000),
                calories = Calories(700),
                menu = Menu(listOf("밥"))
            )
        }

        assertThrows<IllegalArgumentException> {
            Meal.create(
                date = LocalDate.now(),
                diningTime = DiningTime.LUNCH,
                place = "   ",
                price = Money.of(5000),
                calories = Calories(700),
                menu = Menu(listOf("밥"))
            )
        }
    }

    @Test
    fun `일주일 이후의 날짜는 예외가 발생한다`() {
        // given
        val futureDate = LocalDate.now().plusDays(8)

        // when & then
        assertThrows<IllegalArgumentException> {
            Meal.create(
                date = futureDate,
                diningTime = DiningTime.LUNCH,
                place = "학생식당",
                price = Money.of(5000),
                calories = Calories(700),
                menu = Menu(listOf("밥"))
            )
        }
    }

    @Test
    fun `일주일 이내의 날짜는 허용된다`() {
        // given
        val validDate = LocalDate.now().plusDays(7)

        // when
        val meal = Meal.create(
            date = validDate,
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        // then
        assertEquals(validDate, meal.date)
    }

    @Test
    fun `오늘 날짜인지 확인할 수 있다`() {
        // given
        val todayMeal = Meal.create(
            date = LocalDate.now(),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        val tomorrowMeal = Meal.create(
            date = LocalDate.now().plusDays(1),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        // when & then
        assertTrue(todayMeal.isToday())
        assertFalse(tomorrowMeal.isToday())
    }

    @Test
    fun `주말인지 확인할 수 있다`() {
        // given
        val today = LocalDate.now()
        var saturday = today
        while (saturday.dayOfWeek != java.time.DayOfWeek.SATURDAY) {
            saturday = saturday.plusDays(1)
        }
        var sunday = today
        while (sunday.dayOfWeek != java.time.DayOfWeek.SUNDAY) {
            sunday = sunday.plusDays(1)
        }
        var monday = today
        while (monday.dayOfWeek != java.time.DayOfWeek.MONDAY) {
            monday = monday.plusDays(1)
        }

        val saturdayMeal = Meal.create(
            date = saturday,
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        val sundayMeal = Meal.create(
            date = sunday,
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        val mondayMeal = Meal.create(
            date = monday,
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        // when & then
        assertTrue(saturdayMeal.isWeekend())
        assertTrue(sundayMeal.isWeekend())
        assertFalse(mondayMeal.isWeekend())
    }

    @Test
    fun `가격이 6000원을 초과하면 고가격으로 판단한다`() {
        // given
        val highPricedMeal = Meal.create(
            date = LocalDate.now(),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(6001),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        val normalPricedMeal = Meal.create(
            date = LocalDate.now(),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(6000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        // when & then
        assertTrue(highPricedMeal.isHighPriced())
        assertFalse(normalPricedMeal.isHighPriced())
    }

    @Test
    fun `칼로리가 500 미만이면 저칼로리로 판단한다`() {
        // given
        val lowCalorieMeal = Meal.create(
            date = LocalDate.now(),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(499),
            menu = Menu(listOf("샐러드"))
        )

        val normalCalorieMeal = Meal.create(
            date = LocalDate.now(),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(500),
            menu = Menu(listOf("밥"))
        )

        // when & then
        assertTrue(lowCalorieMeal.isLowCalorie())
        assertFalse(normalCalorieMeal.isLowCalorie())
    }

    @Test
    fun `모든 식단은 즐겨찾기가 가능하다`() {
        // given
        val meal = Meal.create(
            date = LocalDate.now(),
            diningTime = DiningTime.LUNCH,
            place = "학생식당",
            price = Money.of(5000),
            calories = Calories(700),
            menu = Menu(listOf("밥"))
        )

        // when & then
        assertTrue(meal.canBeFavorited())
    }

    @Test
    fun `모든 식사 시간에 대해 식단을 생성할 수 있다`() {
        // given
        val date = LocalDate.now()
        val place = "학생식당"
        val price = Money.of(5000)
        val calories = Calories(700)
        val menu = Menu(listOf("밥"))

        // when
        val breakfast = Meal.create(date, DiningTime.BREAKFAST, place, price, calories, menu)
        val lunch = Meal.create(date, DiningTime.LUNCH, place, price, calories, menu)
        val dinner = Meal.create(date, DiningTime.DINNER, place, price, calories, menu)

        // then
        assertEquals(DiningTime.BREAKFAST, breakfast.diningTime)
        assertEquals(DiningTime.LUNCH, lunch.diningTime)
        assertEquals(DiningTime.DINNER, dinner.diningTime)
    }
}

class MealIdTest {

    @Test
    fun `양수 값으로 MealId를 생성할 수 있다`() {
        // given & when
        val mealId = MealId(1L)

        // then
        assertEquals(1L, mealId.value)
    }

    @Test
    fun `0 이하의 값으로 MealId를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            MealId(0L)
        }

        assertThrows<IllegalArgumentException> {
            MealId(-1L)
        }
    }

    @Test
    fun `동일한 값을 가진 MealId는 동등하다`() {
        // given
        val mealId1 = MealId(1L)
        val mealId2 = MealId(1L)

        // when & then
        assertEquals(mealId1, mealId2)
        assertEquals(mealId1.hashCode(), mealId2.hashCode())
    }
}

class DiningTimeTest {

    @Test
    fun `DiningTime enum은 3가지 식사 시간을 가진다`() {
        // when
        val values = DiningTime.values()

        // then
        assertEquals(3, values.size)
        assertTrue(values.contains(DiningTime.BREAKFAST))
        assertTrue(values.contains(DiningTime.LUNCH))
        assertTrue(values.contains(DiningTime.DINNER))
    }

    @Test
    fun `각 DiningTime은 한글 표시명을 가진다`() {
        // when & then
        assertEquals("아침", DiningTime.BREAKFAST.displayName)
        assertEquals("점심", DiningTime.LUNCH.displayName)
        assertEquals("저녁", DiningTime.DINNER.displayName)
    }

    @Test
    fun `문자열로부터 DiningTime을 생성할 수 있다`() {
        // when & then
        assertEquals(DiningTime.BREAKFAST, DiningTime.from("breakfast"))
        assertEquals(DiningTime.LUNCH, DiningTime.from("lunch"))
        assertEquals(DiningTime.DINNER, DiningTime.from("dinner"))
    }

    @Test
    fun `대소문자 구분 없이 DiningTime을 생성할 수 있다`() {
        // when & then
        assertEquals(DiningTime.BREAKFAST, DiningTime.from("BREAKFAST"))
        assertEquals(DiningTime.LUNCH, DiningTime.from("Lunch"))
        assertEquals(DiningTime.DINNER, DiningTime.from("DiNnEr"))
    }

    @Test
    fun `유효하지 않은 문자열은 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            DiningTime.from("invalid")
        }

        assertThrows<IllegalArgumentException> {
            DiningTime.from("")
        }
    }
}
