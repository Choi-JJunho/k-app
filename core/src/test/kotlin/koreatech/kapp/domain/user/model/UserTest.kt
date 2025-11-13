package koreatech.kapp.domain.user.model

import koreatech.kapp.domain.common.Email
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserTest {

    @Test
    fun `create 팩토리 메서드로 새로운 사용자를 생성할 수 있다`() {
        // given
        val email = Email("user@example.com")
        val password = HashedPassword("hashed_password")
        val name = "홍길동"
        val studentEmployeeId = "2024001"

        // when
        val user = User.create(
            email = email,
            password = password,
            name = name,
            studentEmployeeId = studentEmployeeId
        )

        // then
        assertNull(user.id)
        assertEquals(email, user.email)
        assertEquals(password, user.password)
        assertEquals(name, user.name)
        assertEquals(studentEmployeeId, user.studentEmployeeId)
    }

    @Test
    fun `사용자는 ID를 가질 수 있다`() {
        // given
        val userId = UserId(1L)
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        // when
        val userWithId = user.copy(id = userId)

        // then
        assertEquals(userId, userWithId.id)
    }

    @Test
    fun `이름이 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            User.create(
                email = Email("user@example.com"),
                password = HashedPassword("hashed_password"),
                name = "",
                studentEmployeeId = "2024001"
            )
        }

        assertThrows<IllegalArgumentException> {
            User.create(
                email = Email("user@example.com"),
                password = HashedPassword("hashed_password"),
                name = "   ",
                studentEmployeeId = "2024001"
            )
        }
    }

    @Test
    fun `학번이나 사번이 비어있으면 예외가 발생한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            User.create(
                email = Email("user@example.com"),
                password = HashedPassword("hashed_password"),
                name = "홍길동",
                studentEmployeeId = ""
            )
        }

        assertThrows<IllegalArgumentException> {
            User.create(
                email = Email("user@example.com"),
                password = HashedPassword("hashed_password"),
                name = "홍길동",
                studentEmployeeId = "   "
            )
        }
    }

    @Test
    fun `사용자 이름을 업데이트할 수 있다`() {
        // given
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        val originalUpdatedAt = user.updatedAt

        // when
        Thread.sleep(10) // updatedAt 변경을 확인하기 위한 대기
        val updatedUser = user.updateName("김철수")

        // then
        assertEquals("김철수", updatedUser.name)
        assertTrue(updatedUser.updatedAt.isAfter(originalUpdatedAt))
        assertEquals(user.email, updatedUser.email) // 다른 필드는 불변
    }

    @Test
    fun `빈 이름으로 업데이트할 수 없다`() {
        // given
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        // when & then
        assertThrows<IllegalArgumentException> {
            user.updateName("")
        }

        assertThrows<IllegalArgumentException> {
            user.updateName("   ")
        }
    }

    @Test
    fun `비밀번호를 업데이트할 수 있다`() {
        // given
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("old_hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )
        val newPassword = HashedPassword("new_hashed_password")
        val originalUpdatedAt = user.updatedAt

        // when
        Thread.sleep(10) // updatedAt 변경을 확인하기 위한 대기
        val updatedUser = user.updatePassword(newPassword)

        // then
        assertEquals(newPassword, updatedUser.password)
        assertNotEquals(user.password, updatedUser.password)
        assertTrue(updatedUser.updatedAt.isAfter(originalUpdatedAt))
    }

    @Test
    fun `모든 사용자는 활성 상태다`() {
        // given
        val user = User.create(
            email = Email("user@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        // when & then
        assertTrue(user.isActive())
    }
}

class UserIdTest {

    @Test
    fun `양수 값으로 UserId를 생성할 수 있다`() {
        // given & when
        val userId = UserId(1L)

        // then
        assertEquals(1L, userId.value)
    }

    @Test
    fun `0 이하의 값으로 UserId를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            UserId(0L)
        }

        assertThrows<IllegalArgumentException> {
            UserId(-1L)
        }
    }

    @Test
    fun `동일한 값을 가진 UserId는 동등하다`() {
        // given
        val userId1 = UserId(1L)
        val userId2 = UserId(1L)

        // when & then
        assertEquals(userId1, userId2)
        assertEquals(userId1.hashCode(), userId2.hashCode())
    }
}

class HashedPasswordTest {

    @Test
    fun `유효한 해시 값으로 HashedPassword를 생성할 수 있다`() {
        // given & when
        val hashedPassword = HashedPassword("$2a$10$abcdefghijklmnopqrstuv")

        // then
        assertEquals("$2a$10$abcdefghijklmnopqrstuv", hashedPassword.value)
    }

    @Test
    fun `빈 값으로 HashedPassword를 생성하면 예외가 발생한다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            HashedPassword("")
        }

        assertThrows<IllegalArgumentException> {
            HashedPassword("   ")
        }
    }

    @Test
    fun `동일한 해시 값을 가진 HashedPassword는 동등하다`() {
        // given
        val password1 = HashedPassword("hashed_password")
        val password2 = HashedPassword("hashed_password")

        // when & then
        assertEquals(password1, password2)
        assertEquals(password1.hashCode(), password2.hashCode())
    }
}
