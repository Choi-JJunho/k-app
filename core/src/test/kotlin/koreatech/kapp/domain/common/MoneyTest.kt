package koreatech.kapp.domain.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `유효한 금액으로 Money 객체를 생성할 수 있다`() {
        // given & when
        val money = Money(BigDecimal("1000"))

        // then
        assertEquals(BigDecimal("1000"), money.amount)
        assertEquals("KRW", money.currency)
    }

    @Test
    fun `Money 객체를 문자열로 생성할 수 있다`() {
        // given & when
        val money = Money.of("5000")

        // then
        assertEquals(BigDecimal("5000"), money.amount)
    }

    @Test
    fun `Money 객체를 정수로 생성할 수 있다`() {
        // given & when
        val money = Money.of(3000)

        // then
        assertEquals(BigDecimal("3000"), money.amount)
    }

    @Test
    fun `zero 팩토리 메서드로 0원을 생성할 수 있다`() {
        // given & when
        val money = Money.zero()

        // then
        assertEquals(BigDecimal.ZERO, money.amount)
    }

    @Test
    fun `음수 금액은 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Money(BigDecimal("-1000"))
        }
    }

    @Test
    fun `빈 통화는 예외를 발생시킨다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Money(BigDecimal("1000"), "")
        }
    }

    @Test
    fun `동일한 통화의 금액을 더할 수 있다`() {
        // given
        val money1 = Money.of(1000)
        val money2 = Money.of(2000)

        // when
        val result = money1 + money2

        // then
        assertEquals(BigDecimal("3000"), result.amount)
        assertEquals("KRW", result.currency)
    }

    @Test
    fun `동일한 통화의 금액을 뺄 수 있다`() {
        // given
        val money1 = Money.of(5000)
        val money2 = Money.of(2000)

        // when
        val result = money1 - money2

        // then
        assertEquals(BigDecimal("3000"), result.amount)
    }

    @Test
    fun `다른 통화끼리 덧셈을 시도하면 예외가 발생한다`() {
        // given
        val krw = Money(BigDecimal("1000"), "KRW")
        val usd = Money(BigDecimal("10"), "USD")

        // when & then
        assertThrows<IllegalArgumentException> {
            krw + usd
        }
    }

    @Test
    fun `다른 통화끼리 뺄셈을 시도하면 예외가 발생한다`() {
        // given
        val krw = Money(BigDecimal("1000"), "KRW")
        val usd = Money(BigDecimal("10"), "USD")

        // when & then
        assertThrows<IllegalArgumentException> {
            krw - usd
        }
    }

    @Test
    fun `금액이 더 큰지 비교할 수 있다`() {
        // given
        val money1 = Money.of(5000)
        val money2 = Money.of(3000)
        val money3 = Money.of(5000)

        // when & then
        assertTrue(money1.isGreaterThan(money2))
        assertFalse(money2.isGreaterThan(money1))
        assertFalse(money1.isGreaterThan(money3))
    }

    @Test
    fun `다른 통화끼리 크기 비교를 시도하면 예외가 발생한다`() {
        // given
        val krw = Money(BigDecimal("1000"), "KRW")
        val usd = Money(BigDecimal("10"), "USD")

        // when & then
        assertThrows<IllegalArgumentException> {
            krw.isGreaterThan(usd)
        }
    }

    @Test
    fun `toString은 금액과 통화를 반환한다`() {
        // given
        val money = Money(BigDecimal("10000"), "KRW")

        // when
        val result = money.toString()

        // then
        assertEquals("10000KRW", result)
    }

    @Test
    fun `동일한 금액과 통화를 가진 Money 객체는 동등하다`() {
        // given
        val money1 = Money.of(5000)
        val money2 = Money.of(5000)

        // when & then
        assertEquals(money1, money2)
        assertEquals(money1.hashCode(), money2.hashCode())
    }
}
