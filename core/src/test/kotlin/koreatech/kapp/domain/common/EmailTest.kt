package koreatech.kapp.domain.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class EmailTest {

    @Test
    fun `유효한 이메일 주소로 Email 객체를 생성할 수 있다`() {
        // given
        val validEmails = listOf(
            "user@example.com",
            "test.email@domain.co.kr",
            "name+tag@company.org",
            "user123@test-domain.com"
        )

        // when & then
        validEmails.forEach { email ->
            val emailObj = Email(email)
            assertEquals(email, emailObj.value)
            assertEquals(email, emailObj.toString())
        }
    }

    @Test
    fun `유효하지 않은 이메일 형식은 예외를 발생시킨다`() {
        // given
        val invalidEmails = listOf(
            "invalid",
            "invalid@",
            "@domain.com",
            "user@",
            "user domain@example.com",
            "user@domain",
            ""
        )

        // when & then
        invalidEmails.forEach { email ->
            assertThrows<IllegalArgumentException>("$email should be invalid") {
                Email(email)
            }
        }
    }

    @Test
    fun `이메일 주소는 @ 기호와 도메인을 포함해야 한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Email("userdomain.com")
        }
    }

    @Test
    fun `이메일 주소는 최상위 도메인을 포함해야 한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            Email("user@domain")
        }
    }

    @Test
    fun `동일한 이메일 주소를 가진 Email 객체는 동등하다`() {
        // given
        val email1 = Email("test@example.com")
        val email2 = Email("test@example.com")

        // when & then
        assertEquals(email1, email2)
        assertEquals(email1.hashCode(), email2.hashCode())
    }
}
