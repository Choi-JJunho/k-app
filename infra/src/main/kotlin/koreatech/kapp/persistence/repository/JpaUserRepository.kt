package koreatech.kapp.persistence.repository

import koreatech.kapp.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * Spring Data JPA 사용자 리포지토리
 */
interface JpaUserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun existsByEmail(email: String): Boolean
}