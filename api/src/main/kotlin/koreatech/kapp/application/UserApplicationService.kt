package koreatech.kapp.application

import koreatech.kapp.domain.shared.Email
import koreatech.kapp.domain.user.model.*
import koreatech.kapp.domain.user.service.UserDomainService
import koreatech.kapp.dto.*
import koreatech.kapp.service.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 간소화된 사용자 애플리케이션 서비스
 * 도메인 서비스를 조합하여 사용 사례 구현
 */
@Service
@Transactional
class UserApplicationService(
    private val userDomainService: UserDomainService,
    private val jwtService: JwtService
) {

    fun register(request: RegisterRequest): RegisterResponse {
        val email = Email(request.email)
        val user = userDomainService.createUser(
            email = email, 
            rawPassword = request.password,
            name = request.name,
            studentEmployeeId = request.studentEmployeeId
        )

        return RegisterResponse(
            user = user.toUserResponse(),
            message = "회원가입이 완료되었습니다."
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        val email = Email(request.email)
        val user = userDomainService.authenticateUser(email, request.password)

        val token = jwtService.createAccessToken(user.id!!.value)

        return LoginResponse(
            user = user.toUserResponse(),
            token = token
        )
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(userId: UserId): UserResponse {
        val user = userDomainService.getUserById(userId)
        return user.toUserResponse()
    }

    fun updateName(userId: UserId, newName: String): UserResponse {
        val updatedUser = userDomainService.updateUserName(userId, newName)
        return updatedUser.toUserResponse()
    }
}

