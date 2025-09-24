package koreatech.kapp.domain.user.service

import koreatech.kapp.domain.common.DuplicateEmail
import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.common.InvalidCredentials
import koreatech.kapp.domain.common.UserNotFound
import koreatech.kapp.domain.user.model.*
import koreatech.kapp.domain.user.repository.UserRepository

/**
 * 사용자 도메인 서비스
 * 복잡한 비즈니스 규칙 처리
 */
class UserDomainService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(
        email: Email,
        rawPassword: String,
        name: String,
        studentEmployeeId: String
    ): User {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmail(email.value)
        }

        val hashedPassword = passwordEncoder.encode(rawPassword)

        val user = User.create(
            email = email,
            password = HashedPassword(hashedPassword),
            name = name,
            studentEmployeeId = studentEmployeeId
        )

        return userRepository.save(user)
    }

    fun authenticateUser(email: Email, rawPassword: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentials()

        if (!passwordEncoder.matches(rawPassword, user.password.value)) {
            throw InvalidCredentials()
        }

        return user
    }

    fun getUserById(userId: UserId): User {
        return userRepository.findById(userId)
            ?: throw UserNotFound(userId.value.toString())
    }

    fun updateUserName(userId: UserId, newName: String): User {
        val user = getUserById(userId)
        val updatedUser = user.updateName(newName)
        return userRepository.save(updatedUser)
    }
}

/**
 * 패스워드 인코더 인터페이스 (인프라에서 구현)
 */
interface PasswordEncoder {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
