package koreatech.kapp.user.service

import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.domain.user.service.UserDomainService
import koreatech.kapp.user.controller.dto.LoginRequest
import koreatech.kapp.user.controller.dto.LoginResponse
import koreatech.kapp.user.controller.dto.RegisterRequest
import koreatech.kapp.user.controller.dto.RegisterResponse
import koreatech.kapp.user.controller.dto.UserResponse
import koreatech.kapp.user.controller.dto.toUserResponse
import koreatech.kapp.user.jwt.JwtService
import org.springframework.stereotype.Service

/**
 * 간소화된 사용자 애플리케이션 서비스
 * 도메인 서비스를 조합하여 사용 사례 구현
 */
@Service
class AuthService(
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

    fun getCurrentUser(userId: UserId): UserResponse {
        val user = userDomainService.getUserById(userId)
        return user.toUserResponse()
    }
}
