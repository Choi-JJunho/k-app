package koreatech.kapp.domain.user.repository

import koreatech.kapp.domain.shared.Email
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId

/**
 * 사용자 도메인 리포지토리 인터페이스
 * 인프라 계층에서 구현됨
 */
interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun existsByEmail(email: Email): Boolean
    fun delete(user: User)
}