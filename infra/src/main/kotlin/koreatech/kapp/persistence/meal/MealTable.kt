package koreatech.kapp.persistence.meal

import org.jooq.Record
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 식단 테이블 정의 - Kotlin object를 활용한 싱글톤
 */
object MealTable : TableImpl<Record>(DSL.name("meals")) {
    /**
     * 테이블 필드 정의
     */
    val ID: TableField<Record, Long> = createField(DSL.name("id"), SQLDataType.BIGINT.identity(true))
    val DATE: TableField<Record, LocalDate> = createField(DSL.name("date"), SQLDataType.LOCALDATE.nullable(false))
    val DINING_TIME: TableField<Record, String> = createField(DSL.name("dining_time"), SQLDataType.VARCHAR(20).nullable(false))
    val PLACE: TableField<Record, String> = createField(DSL.name("place"), SQLDataType.VARCHAR(100).nullable(false))
    val PRICE: TableField<Record, String> = createField(DSL.name("price"), SQLDataType.VARCHAR(20).nullable(false))
    val KCAL: TableField<Record, String> = createField(DSL.name("kcal"), SQLDataType.VARCHAR(10).nullable(false))
    val CREATED_AT: TableField<Record, LocalDateTime> = createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME.nullable(false))
    val UPDATED_AT: TableField<Record, LocalDateTime> = createField(DSL.name("updated_at"), SQLDataType.LOCALDATETIME.nullable(false))
}

/**
 * 식단 메뉴 아이템 테이블 정의
 */
object MealMenuItemTable : TableImpl<Record>(DSL.name("meal_menu_items")) {
    /**
     * 테이블 필드 정의
     */
    val MEAL_ID: TableField<Record, Long> = createField(DSL.name("meal_id"), SQLDataType.BIGINT.nullable(false))
    val MENU_ITEM: TableField<Record, String> = createField(DSL.name("menu_item"), SQLDataType.VARCHAR(200).nullable(false))
}
