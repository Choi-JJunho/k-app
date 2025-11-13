package koreatech.kapp.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.HashedPassword
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.user.controller.dto.LoginRequest
import koreatech.kapp.user.controller.dto.LoginResponse
import koreatech.kapp.user.controller.dto.RegisterRequest
import koreatech.kapp.user.controller.dto.RegisterResponse
import koreatech.kapp.user.controller.dto.UserResponse
import koreatech.kapp.user.service.AuthService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * AuthController 통합 테스트
 */
@WebMvcTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authService: AuthService

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun authService(): AuthService = mockk(relaxed = true)
    }

    @Test
    fun `회원가입 API 테스트 - 성공`() {
        // given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123",
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        val user = User.create(
            email = Email("test@example.com"),
            password = HashedPassword("hashed_password"),
            name = "홍길동",
            studentEmployeeId = "2024001"
        ).copy(id = UserId(1L))

        val response = RegisterResponse(
            user = UserResponse(
                id = 1L,
                email = "test@example.com",
                name = "홍길동",
                studentEmployeeId = "2024001"
            ),
            message = "회원가입이 완료되었습니다."
        )

        every { authService.register(any()) } returns response

        // when & then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
            .andExpect(jsonPath("$.user.name").value("홍길동"))
            .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))

        verify(exactly = 1) { authService.register(any()) }
    }

    @Test
    fun `로그인 API 테스트 - 성공`() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        val response = LoginResponse(
            user = UserResponse(
                id = 1L,
                email = "test@example.com",
                name = "홍길동",
                studentEmployeeId = "2024001"
            ),
            token = "jwt.token.here"
        )

        every { authService.login(any()) } returns response

        // when & then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
            .andExpect(jsonPath("$.token").value("jwt.token.here"))

        verify(exactly = 1) { authService.login(any()) }
    }

    @Test
    fun `회원가입 API 테스트 - 유효하지 않은 이메일`() {
        // given
        val request = RegisterRequest(
            email = "invalid-email",
            password = "password123",
            name = "홍길동",
            studentEmployeeId = "2024001"
        )

        every { authService.register(any()) } throws IllegalArgumentException("유효하지 않은 이메일 형식입니다")

        // when & then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `로그인 API 테스트 - 잘못된 비밀번호`() {
        // given
        val request = LoginRequest(
            email = "test@example.com",
            password = "wrong_password"
        )

        every { authService.login(any()) } throws IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다")

        // when & then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().is4xxClientError)
    }
}
