package koreatech.kapp.persistence.meal

import koreatech.kapp.domain.meal.model.DiningTime
import koreatech.kapp.domain.meal.model.Meal
import koreatech.kapp.domain.meal.repository.MealRepository
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 식단 도메인 리포지토리 구현체 (JooQ 기반)
 */
@Repository
class DBMealRepository(
    private val dsl: DSLContext
) : MealRepository {

    override fun findByDate(date: LocalDate): List<Meal> {
        return findByDateRecord(date).map { it.toDomain() }
    }

    private fun findByDateRecord(date: LocalDate): List<MealRecord> {
        return dsl.selectFrom(MealTable)
            .where(MealTable.DATE.eq(date))
            .fetch()
            .map { record ->
                val mealId = record[MealTable.ID]
                val menuItems = findMenuItemsByMealId(mealId)
                mapToMealRecord(record, menuItems)
            }
    }

    private fun findMenuItemsByMealId(mealId: Long): List<String> {
        return dsl.select(MealMenuItemTable.MENU_ITEM)
            .from(MealMenuItemTable)
            .where(MealMenuItemTable.MEAL_ID.eq(mealId))
            .fetch()
            .map { it[MealMenuItemTable.MENU_ITEM] }
    }

    private fun mapToMealRecord(record: Record, menuItems: List<String>): MealRecord {
        return MealRecord(
            id = record[MealTable.ID],
            date = record[MealTable.DATE],
            diningTime = DiningTime.valueOf(record[MealTable.DINING_TIME]),
            place = record[MealTable.PLACE],
            price = record[MealTable.PRICE],
            kcal = record[MealTable.KCAL],
            menuItems = menuItems,
            createdAt = record[MealTable.CREATED_AT],
            updatedAt = record[MealTable.UPDATED_AT]
        )
    }
}
