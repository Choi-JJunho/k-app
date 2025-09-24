package koreatech.kapp.global

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(
            message = e.message ?: "잘못된 요청입니다",
            code = "BAD_REQUEST"
        )
    }

    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(e: RuntimeException): ErrorResponse {
        return ErrorResponse(
            message = e.message ?: "서버 내부 오류가 발생했습니다",
            code = "INTERNAL_SERVER_ERROR"
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleException(e: Exception): ErrorResponse {
        return ErrorResponse(
            message = "예상치 못한 오류가 발생했습니다",
            code = "UNEXPECTED_ERROR"
        )
    }
}
