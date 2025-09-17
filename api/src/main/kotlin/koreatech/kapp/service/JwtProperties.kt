package koreatech.kapp.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
data class JwtProperties(
    val secretKey: String,
    val accessTokenExpirationPeriodDay: Long,
)
