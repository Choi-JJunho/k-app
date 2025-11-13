package koreatech.kapp.domain.meal.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MenuTest {

    @Test
    fun `유효한 메뉴 항목 리스트로 Menu 객체를 생성할 수 있다`() {
        // given
        val items = listOf("밥", "김치찌개", "샐러드")

        // when
        val menu = Menu(items)

        // then
        assertEquals(items, menu.items)
    }

    @Test
    fun `빈 메뉴 리스트는 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Menu(emptyList())
        }
    }

    @Test
    fun `빈 문자열이 포함된 메뉴는 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Menu(listOf("밥", "", "김치"))
        }

        assertThrows<IllegalArgumentException> {
            Menu(listOf("밥", "   ", "김치"))
        }
    }

    @Test
    fun `메뉴의 크기를 반환할 수 있다`() {
        // given
        val menu = Menu(listOf("밥", "김치찌개", "샐러드"))

        // when
        val size = menu.size()

        // then
        assertEquals(3, size)
    }

    @Test
    fun `특정 항목이 메뉴에 포함되어 있는지 확인할 수 있다`() {
        // given
        val menu = Menu(listOf("밥", "김치찌개", "샐러드"))

        // when & then
        assertTrue(menu.contains("김치찌개"))
        assertFalse(menu.contains("불고기"))
    }

    @Test
    fun `채식 옵션이 있는지 확인할 수 있다`() {
        // given
        val vegetarianMenu = Menu(listOf("밥", "나물무침", "샐러드"))
        val tofuMenu = Menu(listOf("두부조림", "밥"))
        val mushroomMenu = Menu(listOf("버섯볶음", "콩나물국"))
        val nonVegetarianMenu = Menu(listOf("불고기", "생선구이"))

        // when & then
        assertTrue(vegetarianMenu.hasVegetarianOptions())
        assertTrue(tofuMenu.hasVegetarianOptions())
        assertTrue(mushroomMenu.hasVegetarianOptions())
        assertFalse(nonVegetarianMenu.hasVegetarianOptions())
    }

    @Test
    fun `매운 음식이 있는지 확인할 수 있다`() {
        // given
        val spicyMenu1 = Menu(listOf("매운 돼지불고기", "밥"))
        val spicyMenu2 = Menu(listOf("김치찌개", "밥"))
        val spicyMenu3 = Menu(listOf("고추장찌개", "밥"))
        val mildMenu = Menu(listOf("된장찌개", "밥", "샐러드"))

        // when & then
        assertTrue(spicyMenu1.hasSpicyItems())
        assertTrue(spicyMenu2.hasSpicyItems())
        assertTrue(spicyMenu3.hasSpicyItems())
        assertFalse(mildMenu.hasSpicyItems())
    }

    @Test
    fun `toString은 쉼표로 구분된 메뉴 항목을 반환한다`() {
        // given
        val menu = Menu(listOf("밥", "김치찌개", "샐러드"))

        // when
        val result = menu.toString()

        // then
        assertEquals("밥, 김치찌개, 샐러드", result)
    }

    @Test
    fun `단일 항목 메뉴를 생성할 수 있다`() {
        // given
        val menu = Menu(listOf("김치볶음밥"))

        // when & then
        assertEquals(1, menu.size())
        assertEquals("김치볶음밥", menu.toString())
    }

    @Test
    fun `동일한 메뉴 항목을 가진 Menu 객체는 동등하다`() {
        // given
        val menu1 = Menu(listOf("밥", "김치찌개", "샐러드"))
        val menu2 = Menu(listOf("밥", "김치찌개", "샐러드"))

        // when & then
        assertEquals(menu1, menu2)
        assertEquals(menu1.hashCode(), menu2.hashCode())
    }
}
