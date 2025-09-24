package koreatech.kapp.user.jwt

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("kapp.jwt")
data class JwtProperties(
    val secretKey: String,
    val accessTokenExpirationPeriodDay: Long,
)
