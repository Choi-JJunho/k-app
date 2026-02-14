package koreatech.kapp.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {

    @Test
    fun `같은 통화끼리 합산할 수 있다`() {
        val result = Money.of(3000) + Money.of(2000)

        assertEquals("5000", result.amount.toPlainString())
        assertEquals("KRW", result.currency)
    }

    @Test
    fun `0 미만 금액은 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            Money.of(-1)
        }
    }
}
