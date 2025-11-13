package koreatech.kapp.config

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

/**
 * 모니터링 설정
 * Actuator와 Micrometer를 사용한 애플리케이션 모니터링
 */
@Configuration
class MonitoringConfig {

    /**
     * @Timed 애노테이션을 사용한 메트릭 수집을 위한 Aspect
     */
    @Bean
    fun timedAspect(registry: MeterRegistry): TimedAspect {
        return TimedAspect(registry)
    }
}

/**
 * 데이터베이스 연결 상태를 확인하는 Health Indicator
 */
@Component("database")
class DatabaseHealthIndicator : HealthIndicator {

    override fun health(): Health {
        return try {
            // 실제 구현에서는 데이터베이스 연결을 확인합니다.
            // 예: dataSource.connection.isValid(1)
            Health.up()
                .withDetail("database", "Available")
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}

/**
 * 커스텀 애플리케이션 Health Indicator
 */
@Component("application")
class ApplicationHealthIndicator : HealthIndicator {

    override fun health(): Health {
        // 애플리케이션의 중요한 컴포넌트들의 상태를 확인
        return Health.up()
            .withDetail("status", "Running")
            .withDetail("version", "0.0.1")
            .build()
    }
}
