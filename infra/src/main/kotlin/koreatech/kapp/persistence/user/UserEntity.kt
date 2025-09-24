package koreatech.kapp.persistence.user

import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.HashedPassword
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import java.time.LocalDateTime

/**
 * 사용자 JooQ 레코드
 * 도메인 모델과 분리된 인프라 레벨 레코드
 */
data class UserRecord(
    val id: Long? = null,
    val email: String,
    val password: String,
    val name: String,
    val studentEmployeeId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * JooQ 레코드를 도메인 모델로 변환
     */
    fun toDomain(): User {
        return User(
            id = id?.let { UserId(it) },
            email = Email(email),
            password = HashedPassword(password),
            name = name,
            studentEmployeeId = studentEmployeeId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * 도메인 모델을 JooQ 레코드로 변환
         */
        fun fromDomain(user: User): UserRecord {
            return UserRecord(
                id = user.id?.value,
                email = user.email.value,
                password = user.password.value,
                name = user.name,
                studentEmployeeId = user.studentEmployeeId,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
