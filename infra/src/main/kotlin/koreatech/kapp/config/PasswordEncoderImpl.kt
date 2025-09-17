package koreatech.kapp.config

import koreatech.kapp.domain.user.service.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/**
 * 도메인 PasswordEncoder 인터페이스의 구현체
 */
@Component
class PasswordEncoderImpl : PasswordEncoder {
    private val bCryptPasswordEncoder = BCryptPasswordEncoder()

    override fun encode(rawPassword: String): String {
        return bCryptPasswordEncoder.encode(rawPassword)
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword)
    }
}
