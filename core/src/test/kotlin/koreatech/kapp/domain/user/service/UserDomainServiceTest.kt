package koreatech.kapp.domain.user.service

import io.mockk.*
import koreatech.kapp.domain.common.DuplicateEmail
import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.common.InvalidCredentials
import koreatech.kapp.domain.common.UserNotFound
import koreatech.kapp.domain.user.model.HashedPassword
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserDomainServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userDomainService: UserDomainService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        userDomainService = UserDomainService(userRepository, passwordEncoder)
    }

    @Test
    fun `새로운 사용자를 생성할 수 있다`() {
        // given
        val email = Email("user@example.com")
        val rawPassword = "password123"
        val name = "홍길동"
        val studentEmployeeId = "2024001"
        val encodedPassword = "encoded_password"

        val createdUser = User.create(
            email = email,
            password = HashedPassword(encodedPassword),
            name = name,
            studentEmployeeId = studentEmployeeId
        )
        val savedUser = createdUser.copy(id = UserId(1L))

        every { userRepository.existsByEmail(email) } returns false
        every { passwordEncoder.encode(rawPassword) } returns encodedPassword
        every { userRepository.save(any()) } returns savedUser

        // when
        val result = userDomainService.createUser(email, rawPassword, name, studentEmployeeId)

        // then
        assertNotNull(result.id)
        assertEquals(email, result.email)
        assertEquals(name, result.name)
        assertEquals(studentEmployeeId, result.studentEmployeeId)

        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 1) { passwordEncoder.encode(rawPassword) }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `이미 존재하는 이메일로 사용자를 생성하면 예외가 발생한다`() {
        // given
        val email = Email("existing@example.com")
        val rawPassword = "password123"
        val name = "홍길동"
        val studentEmployeeId = "2024001"

        every { userRepository.existsByEmail(email) } returns true

        // when & then
        assertThrows<DuplicateEmail> {
            userDomainService.createUser(email, rawPassword, name, studentEmployeeId)
        }

        verify(exactly = 1) { userRepository.existsByEmail(email) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `유효한 자격 증명으로 사용자를 인증할 수 있다`() {
        // given
        val email = Email("user@example.com")
        val rawPassword = "password123"
        val encodedPassword = "encoded_password"

        val user = User.create(
            email = email,
            password = HashedPassword(encodedPassword),
            name = "홍길동",
            studentEmployeeId = "2024001"
        ).copy(id = UserId(1L))

        every { userRepository.findByEmail(email) } returns user
        every { passwordEncoder.matches(rawPassword, encodedPassword) } returns true

        // when
        val result = userDomainService.authenticateUser(email, rawPassword)

        // then
        assertEquals(user, result)

        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { passwordEncoder.matches(rawPassword, encodedPassword) }
    }

    @Test
    fun `존재하지 않는 이메일로 인증하면 예외가 발생한다`() {
        // given
        val email = Email("nonexistent@example.com")
        val rawPassword = "password123"

        every { userRepository.findByEmail(email) } returns null

        // when & then
        assertThrows<InvalidCredentials> {
            userDomainService.authenticateUser(email, rawPassword)
        }

        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 0) { passwordEncoder.matches(any(), any()) }
    }

    @Test
    fun `잘못된 비밀번호로 인증하면 예외가 발생한다`() {
        // given
        val email = Email("user@example.com")
        val rawPassword = "wrong_password"
        val encodedPassword = "encoded_password"

        val user = User.create(
            email = email,
            password = HashedPassword(encodedPassword),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        every { userRepository.findByEmail(email) } returns user
        every { passwordEncoder.matches(rawPassword, encodedPassword) } returns false

        // when & then
        assertThrows<InvalidCredentials> {
            userDomainService.authenticateUser(email, rawPassword)
        }

        verify(exactly = 1) { userRepository.findByEmail(email) }
        verify(exactly = 1) { passwordEncoder.matches(rawPassword, encodedPassword) }
    }

    @Test
    fun `ID로 사용자를 조회할 수 있다`() {
        // given
        val userId = UserId(1L)
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("encoded_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        ).copy(id = userId)

        every { userRepository.findById(userId) } returns user

        // when
        val result = userDomainService.getUserById(userId)

        // then
        assertEquals(user, result)
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 예외가 발생한다`() {
        // given
        val userId = UserId(999L)

        every { userRepository.findById(userId) } returns null

        // when & then
        assertThrows<UserNotFound> {
            userDomainService.getUserById(userId)
        }

        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `사용자 이름을 업데이트할 수 있다`() {
        // given
        val userId = UserId(1L)
        val originalUser = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("encoded_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        ).copy(id = userId)

        val newName = "김철수"
        val updatedUser = originalUser.updateName(newName)

        every { userRepository.findById(userId) } returns originalUser
        every { userRepository.save(any()) } returns updatedUser

        // when
        val result = userDomainService.updateUserName(userId, newName)

        // then
        assertEquals(newName, result.name)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `존재하지 않는 사용자의 이름을 업데이트하면 예외가 발생한다`() {
        // given
        val userId = UserId(999L)
        val newName = "김철수"

        every { userRepository.findById(userId) } returns null

        // when & then
        assertThrows<UserNotFound> {
            userDomainService.updateUserName(userId, newName)
        }

        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
