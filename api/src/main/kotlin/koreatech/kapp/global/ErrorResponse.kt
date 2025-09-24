package koreatech.kapp.global

import java.time.LocalDateTime

data class ErrorResponse(
    val message: String,
    val code: String? = null,
    val timestamp: String = LocalDateTime.now().toString()
)
