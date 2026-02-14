package koreatech.kapp.user.service

import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.domain.user.repository.UserRepository
import koreatech.kapp.domain.user.service.PasswordEncoder
import koreatech.kapp.domain.user.service.UserDomainService
import koreatech.kapp.user.controller.dto.LoginRequest
import koreatech.kapp.user.controller.dto.RegisterRequest
import koreatech.kapp.user.jwt.JwtProperties
import koreatech.kapp.user.jwt.JwtService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthServiceTest {

    @Test
    fun `회원가입 후 로그인하면 유효한 JWT를 발급한다`() {
        val repository = InMemoryUserRepository()
        val passwordEncoder = FakePasswordEncoder()
        val userDomainService = UserDomainService(repository, passwordEncoder)
        val jwtService = JwtService(
            JwtProperties(
                secretKey = "auth-test-secret-auth-test-secret-auth-test-secret-auth-test-secret",
                accessTokenExpirationPeriodDay = 1
            )
        )
        val authService = AuthService(userDomainService, jwtService)

        val registerResponse = authService.register(
            RegisterRequest(
                email = "service@example.com",
                password = "pw1234",
                name = "서비스유저",
                studentEmployeeId = "20240099"
            )
        )

        val loginResponse = authService.login(
            LoginRequest(
                email = "service@example.com",
                password = "pw1234"
            )
        )

        assertEquals("회원가입이 완료되었습니다.", registerResponse.message)
        assertEquals("서비스유저", loginResponse.user.name)
        assertTrue(jwtService.isValidToken(loginResponse.token))
    }

    private class FakePasswordEncoder : PasswordEncoder {
        override fun encode(rawPassword: String): String = "encoded:$rawPassword"

        override fun matches(rawPassword: String, encodedPassword: String): Boolean {
            return encodedPassword == "encoded:$rawPassword"
        }
    }

    private class InMemoryUserRepository : UserRepository {
        private val users = linkedMapOf<Long, User>()
        private var sequence = 1L

        override fun save(user: User): User {
            return if (user.id == null) {
                val id = sequence++
                val persisted = user.copy(id = UserId(id))
                users[id] = persisted
                persisted
            } else {
                val id = requireNotNull(user.id).value
                users[id] = user
                user
            }
        }

        override fun findById(id: UserId): User? = users[id.value]

        override fun findByEmail(email: Email): User? = users.values.firstOrNull { it.email == email }

        override fun existsByEmail(email: Email): Boolean = users.values.any { it.email == email }

        override fun delete(user: User) {
            user.id?.let { users.remove(it.value) }
        }
    }
}
