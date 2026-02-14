package koreatech.kapp.domain.user.service

import koreatech.kapp.domain.common.DuplicateEmail
import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.common.InvalidCredentials
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.domain.user.repository.UserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserDomainServiceTest {

    @Test
    fun `중복 이메일로 회원가입하면 예외가 발생한다`() {
        val repository = InMemoryUserRepository()
        val passwordEncoder = FakePasswordEncoder()
        val service = UserDomainService(repository, passwordEncoder)
        repository.save(
            User.create(
                email = Email("dup@example.com"),
                password = koreatech.kapp.domain.user.model.HashedPassword("encoded:pw"),
                name = "기존 사용자",
                studentEmployeeId = "20240001"
            )
        )

        assertFailsWith<DuplicateEmail> {
            service.createUser(
                email = Email("dup@example.com"),
                rawPassword = "newPw",
                name = "신규 사용자",
                studentEmployeeId = "20240002"
            )
        }
    }

    @Test
    fun `정상 로그인 시 사용자를 반환한다`() {
        val repository = InMemoryUserRepository()
        val passwordEncoder = FakePasswordEncoder()
        val service = UserDomainService(repository, passwordEncoder)
        val created = service.createUser(
            email = Email("user@example.com"),
            rawPassword = "pw1234",
            name = "홍길동",
            studentEmployeeId = "20240003"
        )

        val authenticated = service.authenticateUser(Email("user@example.com"), "pw1234")

        assertEquals(created.id, authenticated.id)
        assertEquals("홍길동", authenticated.name)
    }

    @Test
    fun `비밀번호가 다르면 로그인 실패한다`() {
        val repository = InMemoryUserRepository()
        val passwordEncoder = FakePasswordEncoder()
        val service = UserDomainService(repository, passwordEncoder)
        service.createUser(
            email = Email("user@example.com"),
            rawPassword = "pw1234",
            name = "홍길동",
            studentEmployeeId = "20240003"
        )

        assertFailsWith<InvalidCredentials> {
            service.authenticateUser(Email("user@example.com"), "wrong")
        }
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
                val newId = sequence++
                val persisted = user.copy(id = UserId(newId))
                users[newId] = persisted
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
