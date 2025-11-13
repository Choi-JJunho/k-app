package koreatech.kapp.domain.meal.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaloriesTest {

    @Test
    fun `유효한 칼로리 값으로 Calories 객체를 생성할 수 있다`() {
        // given & when
        val calories = Calories(500)

        // then
        assertEquals(500, calories.value)
    }

    @Test
    fun `칼로리는 0일 수 있다`() {
        // given & when
        val calories = Calories(0)

        // then
        assertEquals(0, calories.value)
    }

    @Test
    fun `칼로리는 최대값 9999까지 설정할 수 있다`() {
        // given & when
        val calories = Calories(9999)

        // then
        assertEquals(9999, calories.value)
    }

    @Test
    fun `음수 칼로리는 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Calories(-1)
        }
    }

    @Test
    fun `9999를 초과하는 칼로리는 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Calories(10000)
        }
    }

    @Test
    fun `칼로리를 더할 수 있다`() {
        // given
        val calories1 = Calories(300)
        val calories2 = Calories(200)

        // when
        val result = calories1 + calories2

        // then
        assertEquals(500, result.value)
    }

    @Test
    fun `칼로리를 뺄 수 있다`() {
        // given
        val calories1 = Calories(500)
        val calories2 = Calories(200)

        // when
        val result = calories1 - calories2

        // then
        assertEquals(300, result.value)
    }

    @Test
    fun `800 이상이면 고칼로리로 판단한다`() {
        // given
        val highCalories = Calories(800)
        val veryHighCalories = Calories(1000)
        val normalCalories = Calories(799)

        // when & then
        assertTrue(highCalories.isHighCalorie())
        assertTrue(veryHighCalories.isHighCalorie())
        assertFalse(normalCalories.isHighCalorie())
    }

    @Test
    fun `300 이하면 저칼로리로 판단한다`() {
        // given
        val lowCalories = Calories(300)
        val veryLowCalories = Calories(100)
        val normalCalories = Calories(301)

        // when & then
        assertTrue(lowCalories.isLowCalorie())
        assertTrue(veryLowCalories.isLowCalorie())
        assertFalse(normalCalories.isLowCalorie())
    }

    @Test
    fun `toString은 kcal 단위를 포함한다`() {
        // given
        val calories = Calories(500)

        // when
        val result = calories.toString()

        // then
        assertEquals("500kcal", result)
    }

    @Test
    fun `동일한 칼로리 값을 가진 Calories 객체는 동등하다`() {
        // given
        val calories1 = Calories(500)
        val calories2 = Calories(500)

        // when & then
        assertEquals(calories1, calories2)
        assertEquals(calories1.hashCode(), calories2.hashCode())
    }
}
