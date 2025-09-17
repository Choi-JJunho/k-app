package koreatech.kapp.persistence.entity

import jakarta.persistence.*
import koreatech.kapp.domain.meal.model.*
import koreatech.kapp.domain.shared.Money
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 식단 JPA 엔티티
 */
@Entity
@Table(name = "meals")
data class MealEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val date: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "dining_time", nullable = false)
    val diningTime: DiningTime,

    @Column(nullable = false, length = 100)
    val place: String,

    @Column(nullable = false)
    val price: String,

    @Column(nullable = false)
    val kcal: String,

    @ElementCollection
    @CollectionTable(name = "meal_menu_items", joinColumns = [JoinColumn(name = "meal_id")])
    @Column(name = "menu_item")
    val menu: List<String>,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * JPA 엔티티를 도메인 모델로 변환
     */
    fun toDomain(): Meal {
        return Meal(
            id = id?.let { MealId(it) },
            date = date,
            diningTime = diningTime,
            place = place,
            price = Money.of(price),
            calories = Calories(kcal.toInt()),
            menu = Menu(menu),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * 도메인 모델을 JPA 엔티티로 변환
         */
        fun fromDomain(meal: Meal): MealEntity {
            return MealEntity(
                id = meal.id?.value,
                date = meal.date,
                diningTime = meal.diningTime,
                place = meal.place,
                price = meal.price.amount.toString(),
                kcal = meal.calories.value.toString(),
                menu = meal.menu.items,
                createdAt = meal.createdAt,
                updatedAt = meal.updatedAt
            )
        }
    }
}
