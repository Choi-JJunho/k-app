package koreatech.kapp.user

import koreatech.kapp.domain.user.repository.UserRepository
import koreatech.kapp.domain.user.service.PasswordEncoder
import koreatech.kapp.domain.user.service.UserDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserBeanConfig {

    @Bean
    fun userDomainService(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): UserDomainService {
        return UserDomainService(userRepository, passwordEncoder)
    }
}
