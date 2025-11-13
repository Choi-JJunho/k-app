package koreatech.kapp.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * Rate Limiting 설정
 * API 엔드포인트별로 요청 제한을 설정합니다.
 */
@Configuration
class RateLimitingConfig : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(RateLimitInterceptor())
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/health", "/actuator/**")
    }
}

/**
 * Rate Limiting 인터셉터
 * IP 주소 기반으로 요청을 제한합니다.
 */
class RateLimitInterceptor : org.springframework.web.servlet.HandlerInterceptor {

    private val cache = ConcurrentHashMap<String, Bucket>()

    override fun preHandle(
        request: jakarta.servlet.http.HttpServletRequest,
        response: jakarta.servlet.http.HttpServletResponse,
        handler: Any
    ): Boolean {
        val ip = getClientIP(request)
        val bucket = resolveBucket(ip)

        val probe = bucket.tryConsumeAndReturnRemaining(1)

        return if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            true
        } else {
            val waitForRefill = probe.nanosToWaitForRefill / 1_000_000_000
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitForRefill.toString())
            response.sendError(429, "Too many requests. Please try again later.")
            false
        }
    }

    private fun resolveBucket(ip: String): Bucket {
        return cache.computeIfAbsent(ip) { createNewBucket() }
    }

    private fun createNewBucket(): Bucket {
        // 분당 60개 요청 허용 (초당 1개)
        val limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)))
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }

    private fun getClientIP(request: jakarta.servlet.http.HttpServletRequest): String {
        val xfHeader = request.getHeader("X-Forwarded-For")
        return if (xfHeader.isNullOrEmpty()) {
            request.remoteAddr
        } else {
            xfHeader.split(",")[0]
        }
    }
}
