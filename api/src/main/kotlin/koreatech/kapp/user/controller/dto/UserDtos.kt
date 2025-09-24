package koreatech.kapp.user.controller.dto

import koreatech.kapp.domain.user.model.User
import java.time.LocalDateTime

/**
 * 간소화된 사용자 관련 DTO 클래스들
 */
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val user: UserResponse,
    val token: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val studentEmployeeId: String
)

data class RegisterResponse(
    val user: UserResponse,
    val message: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val studentEmployeeId: String,
    val createdAt: LocalDateTime
)

data class ErrorResponse(
    val error: String
)

// Extension function for conversion
fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = id?.value?.toString() ?: "",
        email = email.value,
        name = name,
        studentEmployeeId = studentEmployeeId,
        createdAt = createdAt
    )
}
