package koreatech.kapp.persistence.repository

import koreatech.kapp.domain.shared.Email
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.domain.user.repository.UserRepository
import koreatech.kapp.persistence.entity.UserEntity
import org.springframework.stereotype.Repository

/**
 * 도메인 UserRepository 인터페이스의 JPA 구현체
 */
@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = jpaUserRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: UserId): User? {
        return jpaUserRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        return jpaUserRepository.findByEmail(email.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun existsByEmail(email: Email): Boolean {
        return jpaUserRepository.existsByEmail(email.value)
    }

    override fun delete(user: User) {
        user.id?.let { userId ->
            jpaUserRepository.deleteById(userId.value)
        }
    }
}
