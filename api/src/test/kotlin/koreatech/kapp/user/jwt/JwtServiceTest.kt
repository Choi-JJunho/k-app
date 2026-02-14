package koreatech.kapp.user.jwt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtServiceTest {

    private val jwtService = JwtService(
        JwtProperties(
            secretKey = "test-secret-key-test-secret-key-test-secret-key-test-secret-key",
            accessTokenExpirationPeriodDay = 1
        )
    )

    @Test
    fun `토큰 생성 후 사용자 ID를 추출할 수 있다`() {
        val token = jwtService.createAccessToken(123L)
        val extractedId = jwtService.extractUserId(token)

        assertEquals(123L, extractedId)
    }

    @Test
    fun `유효하지 않은 토큰은 false를 반환한다`() {
        assertFalse(jwtService.isValidToken("invalid-token"))
    }

    @Test
    fun `생성된 토큰은 유효하다`() {
        val token = jwtService.createAccessToken(321L)

        assertTrue(jwtService.isValidToken(token))
    }
}
