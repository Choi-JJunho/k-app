package koreatech.kapp.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 간소화된 JWT 토큰 생성 및 검증 서비스
 */
@Service
class JwtService(
    private val jwtProperties: JwtProperties,
) {
    private val algorithm: Algorithm = Algorithm.HMAC512(jwtProperties.secretKey)
    private val accessTokenExpirationMillis: Long =
        TimeUnit.MILLISECONDS.convert(jwtProperties.accessTokenExpirationPeriodDay, TimeUnit.DAYS)

    fun createAccessToken(userId: Long): String {
        return JWT.create()
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpirationMillis))
            .withClaim(USER_ID_CLAIM, userId)
            .withIssuedAt(Date())
            .sign(algorithm)
    }

    fun extractUserId(token: String): Long {
        return try {
            JWT.require(algorithm)
                .build()
                .verify(token)
                .getClaim(USER_ID_CLAIM)
                .asLong()
        } catch (e: JWTVerificationException) {
            throw IllegalArgumentException("Invalid JWT token", e)
        }
    }

    fun isValidToken(token: String): Boolean {
        return try {
            JWT.require(algorithm)
                .build()
                .verify(token)
            true
        } catch (e: JWTVerificationException) {
            false
        }
    }

    companion object {
        private const val USER_ID_CLAIM = "userId"
    }
}
