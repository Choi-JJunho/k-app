package koreatech.kapp.auth

import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.user.jwt.JwtService
import koreatech.kapp.user.service.AuthService
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

private const val BEARER_PREFIX = "Bearer "

/**
 * JWT 토큰 검증을 통한 사용자 인증 ArgumentResolver
 * Authorization 헤더의 Bearer 토큰을 검증하고 사용자 ID를 주입
 */
@Component
class AuthenticatedUserArgumentResolver(
    private val jwtService: JwtService,
    private val authService: AuthService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthenticatedUser::class.java) &&
                parameter.parameterType == User::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): User {
        val authorizationHeader = webRequest.getHeader("Authorization")
            ?: throw IllegalArgumentException("Authorization header is required")

        require(authorizationHeader.startsWith(BEARER_PREFIX)) { "Authorization header must start with 'Bearer '" }

        val token = authorizationHeader.substring(BEARER_PREFIX.length)

        require(jwtService.isValidToken(token)) { "Invalid JWT token" }

        val extractedId = jwtService.extractUserId(token)
        val userId = UserId(extractedId)
        val user = try {
            authService.getCurrentUser(userId)
        } catch (e: Exception) {
            throw IllegalArgumentException("부정확한 토큰입니다.")
        }
        return user
    }
}
