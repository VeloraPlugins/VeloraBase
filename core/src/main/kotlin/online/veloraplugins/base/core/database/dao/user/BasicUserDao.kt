package online.veloraplugins.base.core.database.dao.user

import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.database.dao.BaseDao
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import java.util.*

class BasicUserDao(
    db: DatabaseService
) : BaseDao<BasicUserDao.Users>(db, Users) {

    object Users : Table("users") {
        val uuid = uuid("uuid")
        val name = varchar("name", 32)
        val coins = integer("coins").default(0)

        override val primaryKey = PrimaryKey(uuid)
    }

    suspend fun createUser(uuid: UUID, name: String) = query {
        Users.insertIgnore {
            it[Users.uuid] = uuid
            it[Users.name] = name
        }
    }

    /** Wrapper voor BaseDao.exists() */
    suspend fun exists(uuid: UUID): Boolean =
        exists { Users.uuid eq uuid }

    suspend fun exists(name: String): Boolean =
        exists { Users.name eq name }

    suspend fun getUser(uuid: UUID): UserData? = query {
        Users
            .select { Users.uuid eq uuid }
            .limit(1)
            .singleOrNull()
            ?.let {
                UserData(
                    uuid = it[Users.uuid],
                    name = it[Users.name],
                    coins = it[Users.coins]
                )
            }
    }
}
