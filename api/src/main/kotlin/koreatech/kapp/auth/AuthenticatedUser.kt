package koreatech.kapp.auth

import io.swagger.v3.oas.annotations.Hidden

/**
 * 인증된 사용자 정보를 담는 어노테이션
 * ArgumentResolver에서 JWT 토큰을 검증 후 사용자 ID를 주입할 때 사용
 */
@Hidden
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthenticatedUser
