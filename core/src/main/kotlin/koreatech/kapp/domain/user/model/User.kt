package koreatech.kapp.domain.user.model

import koreatech.kapp.domain.common.Email
import java.time.LocalDateTime

/**
 * 사용자 집계 루트 (Aggregate Root)
 * 간소화된 사용자 정보 관리
 */
data class User(
    val id: UserId?,
    val email: Email,
    val password: HashedPassword,
    val name: String,
    val studentEmployeeId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(name.isNotBlank()) { "이름은 비어있을 수 없습니다." }
        require(studentEmployeeId.isNotBlank()) { "학번/사번은 비어있을 수 없습니다." }
    }

    fun updateName(newName: String): User {
        require(newName.isNotBlank()) { "이름은 비어있을 수 없습니다." }
        return copy(
            name = newName,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updatePassword(newPassword: HashedPassword): User {
        return copy(
            password = newPassword,
            updatedAt = LocalDateTime.now()
        )
    }

    fun isActive(): Boolean = true

    companion object {
        fun create(
            email: Email,
            password: HashedPassword,
            name: String,
            studentEmployeeId: String
        ): User {
            return User(
                id = null,
                email = email,
                password = password,
                name = name,
                studentEmployeeId = studentEmployeeId
            )
        }
    }
}

data class UserId(val value: Long) {
    init {
        require(value > 0) { "사용자 ID는 0보다 커야 합니다." }
    }
}

data class HashedPassword(val value: String) {
    init {
        require(value.isNotBlank()) { "해시된 비밀번호는 비어있을 수 없습니다." }
    }
}
