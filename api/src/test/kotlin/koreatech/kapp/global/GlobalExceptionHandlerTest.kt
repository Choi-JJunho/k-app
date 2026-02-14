package koreatech.kapp.global

import koreatech.kapp.domain.common.DuplicateEmail
import koreatech.kapp.domain.common.InvalidCredentials
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `중복 이메일 예외는 409로 매핑된다`() {
        val response = handler.handleDomainException(DuplicateEmail("dup@example.com"))

        assertEquals(409, response.statusCode.value())
        assertEquals("DOMAIN_ERROR", response.body?.code)
    }

    @Test
    fun `인증 실패 예외는 401로 매핑된다`() {
        val response = handler.handleDomainException(InvalidCredentials())

        assertEquals(401, response.statusCode.value())
        assertEquals("DOMAIN_ERROR", response.body?.code)
    }

    @Test
    fun `UnauthorizedException은 401 응답 본문을 반환한다`() {
        val body = handler.handleUnauthorizedException(UnauthorizedException("invalid token"))

        assertEquals("UNAUTHORIZED", body.code)
        assertEquals("invalid token", body.message)
    }
}
