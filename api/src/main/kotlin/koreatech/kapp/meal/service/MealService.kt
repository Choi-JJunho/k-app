package koreatech.kapp.meal.service

import koreatech.kapp.domain.meal.service.MealDomainService
import org.springframework.stereotype.Service

@Service
class MealService(
    private val mealDomainService: MealDomainService
) {

}
