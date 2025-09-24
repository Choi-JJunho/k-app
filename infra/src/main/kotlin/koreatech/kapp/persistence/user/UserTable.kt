package koreatech.kapp.persistence.user

import org.jooq.Record
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl
import java.time.LocalDateTime

object UserTable : TableImpl<Record>(DSL.name("users")) {
    val ID: TableField<Record, Long> =
        createField(DSL.name("id"), SQLDataType.BIGINT.identity(true))
    val EMAIL: TableField<Record, String> =
        createField(DSL.name("email"), SQLDataType.VARCHAR(255).nullable(false))
    val PASSWORD: TableField<Record, String> =
        createField(DSL.name("password"), SQLDataType.VARCHAR(255).nullable(false))
    val NAME: TableField<Record, String> =
        createField(DSL.name("name"), SQLDataType.VARCHAR(100).nullable(false))
    val STUDENT_EMPLOYEE_ID: TableField<Record, String> =
        createField(DSL.name("student_employee_id"), SQLDataType.VARCHAR(50).nullable(false))
    val CREATED_AT: TableField<Record, LocalDateTime> =
        createField(DSL.name("created_at"), SQLDataType.LOCALDATETIME.nullable(false))
    val UPDATED_AT: TableField<Record, LocalDateTime> =
        createField(DSL.name("updated_at"), SQLDataType.LOCALDATETIME.nullable(false))
}
