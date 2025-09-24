package koreatech.kapp.meal

import koreatech.kapp.domain.meal.repository.MealRepository
import koreatech.kapp.domain.meal.service.MealDomainService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MealBeanConfig {

    @Bean
    fun mealDomainService(
        mealRepository: MealRepository
    ): MealDomainService {
        return MealDomainService(mealRepository)
    }
}
