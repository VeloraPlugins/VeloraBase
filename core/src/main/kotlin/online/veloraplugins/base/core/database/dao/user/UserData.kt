package online.veloraplugins.base.core.database.dao.user

import java.util.*

/**
 * Represents a simple user record stored in the database.
 *
 * This is a plain Kotlin data class with no logic inside.
 * DAOs (such as BasicUserDao) map database rows into this structure.
 */
data class UserData(
    val uuid: UUID,
    val name: String,
    val coins: Int
)