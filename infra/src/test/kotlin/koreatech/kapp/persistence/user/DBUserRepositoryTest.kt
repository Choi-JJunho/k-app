package koreatech.kapp.persistence.user

import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.HashedPassword
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * DBUserRepository 통합 테스트
 * 실제 데이터베이스(H2)와 JooQ를 사용한 통합 테스트
 */
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DBUserRepositoryTest {

    @Autowired
    private lateinit var dsl: DSLContext

    private lateinit var repository: DBUserRepository

    @AfterEach
    fun cleanup() {
        // 각 테스트 후 데이터 정리
        dsl.deleteFrom(UserTable).execute()
    }

    private fun setupRepository() {
        repository = DBUserRepository(dsl)
    }

    @Test
    fun `새로운 사용자를 저장할 수 있다`() {
        // given
        setupRepository()
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        // when
        val savedUser = repository.save(user)

        // then
        assertNotNull(savedUser.id)
        assertEquals(user.email, savedUser.email)
        assertEquals(user.password, savedUser.password)
        assertEquals(user.name, savedUser.name)
        assertEquals(user.studentEmployeeId, savedUser.studentEmployeeId)
    }

    @Test
    fun `ID로 사용자를 조회할 수 있다`() {
        // given
        setupRepository()
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        val savedUser = repository.save(user)

        // when
        val foundUser = repository.findById(savedUser.id!!)

        // then
        assertNotNull(foundUser)
        assertEquals(savedUser.id, foundUser.id)
        assertEquals(savedUser.email, foundUser.email)
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 null을 반환한다`() {
        // given
        setupRepository()
        val nonExistentId = UserId(999L)

        // when
        val foundUser = repository.findById(nonExistentId)

        // then
        assertNull(foundUser)
    }

    @Test
    fun `이메일로 사용자를 조회할 수 있다`() {
        // given
        setupRepository()
        val email = Email("user@example.com")
        val user = User.create(
            email = email,
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        repository.save(user)

        // when
        val foundUser = repository.findByEmail(email)

        // then
        assertNotNull(foundUser)
        assertEquals(email, foundUser.email)
    }

    @Test
    fun `존재하지 않는 이메일로 조회하면 null을 반환한다`() {
        // given
        setupRepository()
        val nonExistentEmail = Email("nonexistent@example.com")

        // when
        val foundUser = repository.findByEmail(nonExistentEmail)

        // then
        assertNull(foundUser)
    }

    @Test
    fun `이메일 존재 여부를 확인할 수 있다`() {
        // given
        setupRepository()
        val email = Email("user@example.com")
        val user = User.create(
            email = email,
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        repository.save(user)

        // when & then
        assertTrue(repository.existsByEmail(email))
        assertTrue(!repository.existsByEmail(Email("nonexistent@example.com")))
    }

    @Test
    fun `기존 사용자를 업데이트할 수 있다`() {
        // given
        setupRepository()
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        val savedUser = repository.save(user)

        // when
        val updatedUser = savedUser.updateName("김철수")
        val result = repository.save(updatedUser)

        // then
        val foundUser = repository.findById(savedUser.id!!)
        assertNotNull(foundUser)
        assertEquals("김철수", foundUser.name)
        assertEquals(savedUser.id, foundUser.id)
    }

    @Test
    fun `사용자를 삭제할 수 있다`() {
        // given
        setupRepository()
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        val savedUser = repository.save(user)

        // when
        repository.delete(savedUser)

        // then
        val foundUser = repository.findById(savedUser.id!!)
        assertNull(foundUser)
    }

    @Test
    fun `여러 사용자를 저장하고 조회할 수 있다`() {
        // given
        setupRepository()
        val user1 = User.create(
            email = Email("user1@example.com"),
            password = HashedPassword("password1"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        val user2 = User.create(
            email = Email("user2@example.com"),
            password = HashedPassword("password2"),
            name = "김철수",
            studentEmployeeId = "2024002"
        )

        // when
        val savedUser1 = repository.save(user1)
        val savedUser2 = repository.save(user2)

        // then
        assertNotNull(repository.findById(savedUser1.id!!))
        assertNotNull(repository.findById(savedUser2.id!!))
        assertNotNull(repository.findByEmail(Email("user1@example.com")))
        assertNotNull(repository.findByEmail(Email("user2@example.com")))
    }

    @Test
    fun `동일한 이메일을 가진 사용자가 이미 존재하는지 확인할 수 있다`() {
        // given
        setupRepository()
        val email = Email("user@example.com")
        val user = User.create(
            email = email,
            password = HashedPassword("password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        repository.save(user)

        // when & then
        assertTrue(repository.existsByEmail(email))
    }
}
