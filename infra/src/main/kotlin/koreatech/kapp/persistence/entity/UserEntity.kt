package koreatech.kapp.persistence.entity

import jakarta.persistence.*
import koreatech.kapp.domain.shared.Email
import koreatech.kapp.domain.user.model.*
import java.time.LocalDateTime

/**
 * 사용자 JPA 엔티티
 * 도메인 모델과 분리된 인프라 레벨 엔티티
 */
@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val name: String,

    @Column(name = "student_employee_id", nullable = false)
    val studentEmployeeId: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
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
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
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
