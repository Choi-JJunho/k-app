package koreatech.kapp.user.controller

import io.swagger.v3.oas.annotations.Operation
import koreatech.kapp.auth.AuthenticatedUser
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.user.controller.dto.LoginRequest
import koreatech.kapp.user.controller.dto.LoginResponse
import koreatech.kapp.user.controller.dto.RegisterRequest
import koreatech.kapp.user.controller.dto.RegisterResponse
import koreatech.kapp.user.controller.dto.UserResponse
import koreatech.kapp.user.controller.dto.toUserResponse
import koreatech.kapp.user.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API 엔드포인트
 * 간소화된 JWT 토큰 기반 인증 시스템
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userApplicationService: AuthService
) {

    @Operation(description = "회원가입")
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        val response = userApplicationService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(description = "로그인")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val response = userApplicationService.login(request)
        return ResponseEntity.ok(response)
    }

    @Operation(description = "로그인중인 회원 조회")
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticatedUser userId: UserId
    ): ResponseEntity<UserResponse> {
        val user = userApplicationService.getCurrentUser(userId)
        return ResponseEntity.ok(user.toUserResponse())
    }
}
