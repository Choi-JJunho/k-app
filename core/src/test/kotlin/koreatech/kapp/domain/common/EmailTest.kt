package koreatech.kapp.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmailTest {

    @Test
    fun `유효한 이메일은 생성된다`() {
        val email = Email("user@example.com")

        assertEquals("user@example.com", email.value)
    }

    @Test
    fun `유효하지 않은 이메일은 예외를 던진다`() {
        assertFailsWith<IllegalArgumentException> {
            Email("invalid-email")
        }
    }
}
