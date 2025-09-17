package koreatech.kapp.controller

import koreatech.kapp.application.UserApplicationService
import koreatech.kapp.auth.AuthenticatedUser
import koreatech.kapp.domain.shared.DomainException
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.dto.*
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
    private val userApplicationService: UserApplicationService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val response = userApplicationService.register(request)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: DomainException) {
            val status = when (e) {
                is koreatech.kapp.domain.shared.DuplicateEmail -> HttpStatus.CONFLICT
                else -> HttpStatus.BAD_REQUEST
            }
            ResponseEntity.status(status).body(ErrorResponse(e.message ?: "회원가입 중 오류가 발생했습니다."))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ErrorResponse("회원가입 중 오류가 발생했습니다."))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            val response = userApplicationService.login(request)
            ResponseEntity.ok(response)
        } catch (e: DomainException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("이메일 또는 비밀번호가 올바르지 않습니다."))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("로그인 중 오류가 발생했습니다."))
        }
    }

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticatedUser userId: UserId
    ): ResponseEntity<Any> {
        return try {
            val response = userApplicationService.getCurrentUser(userId)
            ResponseEntity.ok(response)
        } catch (e: DomainException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse(e.message ?: "사용자 정보를 가져올 수 없습니다."))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("서버 오류가 발생했습니다."))
        }
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Any> {
        // JWT는 stateless이므로 클라이언트에서 토큰을 제거하면 됨
        return ResponseEntity.ok(mapOf("message" to "로그아웃되었습니다."))
    }
}
