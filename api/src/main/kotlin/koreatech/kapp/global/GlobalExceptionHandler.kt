package koreatech.kapp.global

import io.github.oshai.kotlinlogging.KotlinLogging
import koreatech.kapp.domain.common.DomainException
import koreatech.kapp.domain.common.DuplicateEmail
import koreatech.kapp.domain.common.InvalidCredentials
import koreatech.kapp.domain.common.MealNotFound
import koreatech.kapp.domain.common.UserNotFound
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ResponseEntity<ErrorResponse> {
        logger.warn { e.message }

        val status = when (e) {
            is InvalidCredentials -> HttpStatus.UNAUTHORIZED
            is UserNotFound, is MealNotFound -> HttpStatus.NOT_FOUND
            is DuplicateEmail -> HttpStatus.CONFLICT
            else -> HttpStatus.BAD_REQUEST
        }

        return ResponseEntity.status(status).body(
            ErrorResponse(
                message = e.message ?: "비즈니스 규칙 위반이 발생했습니다",
                code = "DOMAIN_ERROR"
            )
        )
    }

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(e: UnauthorizedException): ErrorResponse {
        logger.warn { e.message }
        return ErrorResponse(
            message = e.message ?: "인증이 필요합니다",
            code = "UNAUTHORIZED"
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ErrorResponse {
        logger.warn { e.message }
        return ErrorResponse(
            message = e.message ?: "잘못된 요청입니다",
            code = "BAD_REQUEST"
        )
    }

    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(e: RuntimeException): ErrorResponse {
        logger.error { e.stackTraceToString() }
        return ErrorResponse(
            message = e.message ?: "서버 내부 오류가 발생했습니다",
            code = "INTERNAL_SERVER_ERROR"
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ErrorResponse {
        logger.error { e.stackTraceToString() }
        return ErrorResponse(
            message = "예상치 못한 오류가 발생했습니다",
            code = "UNEXPECTED_ERROR"
        )
    }
}
