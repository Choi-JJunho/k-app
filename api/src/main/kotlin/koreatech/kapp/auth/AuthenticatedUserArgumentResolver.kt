package koreatech.kapp.auth

import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.service.JwtService
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

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
    ): Any {
        val authorizationHeader = webRequest.getHeader("Authorization")
            ?: throw IllegalArgumentException("Authorization header is required")

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Authorization header must start with 'Bearer '")
        }

        val token = authorizationHeader.substring(7) // "Bearer " 제거

        if (!jwtService.isValidToken(token)) {
            throw IllegalArgumentException("Invalid JWT token")
        }

        val userId = jwtService.extractUserId(token)
        return UserId(userId)
    }
}