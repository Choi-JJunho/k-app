package koreatech.kapp.config

import koreatech.kapp.auth.AuthenticatedUserArgumentResolver
import koreatech.kapp.domain.user.repository.UserRepository
import koreatech.kapp.domain.user.service.PasswordEncoder
import koreatech.kapp.domain.user.service.UserDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@ComponentScan(basePackages = ["koreatech.kapp"])
class ApplicationConfig(
    private val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver
) : WebMvcConfigurer {

    @Bean
    fun userDomainService(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): UserDomainService {
        return UserDomainService(userRepository, passwordEncoder)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}
