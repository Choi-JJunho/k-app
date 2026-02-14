package koreatech.kapp.config

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordEncoderImplTest {

    private val passwordEncoder = PasswordEncoderImpl()

    @Test
    fun `비밀번호를 해시하고 검증할 수 있다`() {
        val rawPassword = "my-password"
        val encoded = passwordEncoder.encode(rawPassword)

        assertTrue(passwordEncoder.matches(rawPassword, encoded))
        assertFalse(passwordEncoder.matches("wrong-password", encoded))
    }
}
