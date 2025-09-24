package koreatech.kapp.persistence.user

import koreatech.kapp.domain.common.Email
import koreatech.kapp.domain.user.model.User
import koreatech.kapp.domain.user.model.UserId
import koreatech.kapp.domain.user.repository.UserRepository
import org.jooq.DSLContext
import org.jooq.Record
import org.springframework.stereotype.Repository

/**
 * 사용자 도메인 리포지토리 구현체 (JooQ 기반)
 */
@Repository
class DBUserRepository(
    private val dsl: DSLContext
) : UserRepository {

    override fun save(user: User): User {
        val userRecord = UserRecord.fromDomain(user)
        val savedRecord = saveRecord(userRecord)
        return savedRecord.toDomain()
    }

    override fun findById(id: UserId): User? {
        return findByIdRecord(id.value)?.toDomain()
    }

    override fun findByEmail(email: Email): User? {
        return findByEmailRecord(email.value)?.toDomain()
    }

    override fun existsByEmail(email: Email): Boolean {
        return existsByEmailRecord(email.value)
    }

    override fun delete(user: User) {
        user.id?.let { deleteById(it.value) }
    }

    // Record 기반 메서드들 (내부 구현)

    private fun findByIdRecord(id: Long): UserRecord? {
        return dsl.selectFrom(UserTable)
            .where(UserTable.ID.eq(id))
            .fetchOptional()
            .map { mapToUserRecord(it) }
            .orElse(null)
    }

    private fun findByEmailRecord(email: String): UserRecord? {
        return dsl.selectFrom(UserTable)
            .where(UserTable.EMAIL.eq(email))
            .fetchOptional()
            .map { mapToUserRecord(it) }
            .orElse(null)
    }

    private fun existsByEmailRecord(email: String): Boolean {
        return dsl.selectCount()
            .from(UserTable)
            .where(UserTable.EMAIL.eq(email))
            .fetchOne(0, Int::class.java)!! > 0
    }

    private fun saveRecord(userRecord: UserRecord): UserRecord {
        return if (userRecord.id == null) {
            // Insert new record
            val newId = dsl.insertInto(UserTable)
                .set(UserTable.EMAIL, userRecord.email)
                .set(UserTable.PASSWORD, userRecord.password)
                .set(UserTable.NAME, userRecord.name)
                .set(UserTable.STUDENT_EMPLOYEE_ID, userRecord.studentEmployeeId)
                .set(UserTable.CREATED_AT, userRecord.createdAt)
                .set(UserTable.UPDATED_AT, userRecord.updatedAt)
                .returningResult(UserTable.ID)
                .fetchOne()?.value1()

            userRecord.copy(id = newId)
        } else {
            // Update existing record
            dsl.update(UserTable)
                .set(UserTable.EMAIL, userRecord.email)
                .set(UserTable.PASSWORD, userRecord.password)
                .set(UserTable.NAME, userRecord.name)
                .set(UserTable.STUDENT_EMPLOYEE_ID, userRecord.studentEmployeeId)
                .set(UserTable.UPDATED_AT, userRecord.updatedAt)
                .where(UserTable.ID.eq(userRecord.id))
                .execute()

            userRecord
        }
    }

    private fun deleteById(id: Long) {
        dsl.deleteFrom(UserTable)
            .where(UserTable.ID.eq(id))
            .execute()
    }

    private fun mapToUserRecord(record: Record): UserRecord {
        return UserRecord(
            id = record[UserTable.ID],
            email = record[UserTable.EMAIL],
            password = record[UserTable.PASSWORD],
            name = record[UserTable.NAME],
            studentEmployeeId = record[UserTable.STUDENT_EMPLOYEE_ID],
            createdAt = record[UserTable.CREATED_AT],
            updatedAt = record[UserTable.UPDATED_AT]
        )
    }
}
