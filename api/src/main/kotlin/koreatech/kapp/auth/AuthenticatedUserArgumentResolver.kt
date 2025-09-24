package koreatech.kapp.auth

import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.user.jwt.JwtService
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
    private val jwtService: JwtService
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthenticatedUser::class.java) &&
                parameter.parameterType == UserId::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): UserId {
        val authorizationHeader = webRequest.getHeader("Authorization")
            ?: throw IllegalArgumentException("Authorization header is required")

        require(authorizationHeader.startsWith(BEARER_PREFIX)) { "Authorization header must start with 'Bearer '" }

        val token = authorizationHeader.substring(BEARER_PREFIX.length)

        require(jwtService.isValidToken(token)) { "Invalid JWT token" }

        val userId = jwtService.extractUserId(token)
        return UserId(userId)
    }
}
