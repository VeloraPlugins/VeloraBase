package online.veloraplugins.base.core.database.core

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.dao.BaseDao
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.core.service.Service
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class SchemaService(
    private val app: BasePlugin,
) : AbstractService(app) {

    override val dependsOn: Set<KClass<out Service>> =
        setOf(DatabaseService::class)

    /** Cache: DAO class → DAO instance */
    private val daoCache = mutableMapOf<KClass<out BaseDao<*>>, BaseDao<*>>()

    fun getDatabase(): DatabaseService =
        app.serviceManager.require(DatabaseService::class)

    /**
     * Returns or instantiates the DAO for the given class.
     * Automatically creates missing tables.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseDao<*>> getSchema(clazz: KClass<T>): T {
        // Already exists in cache
        daoCache[clazz]?.let { return it as T }

        val db = getDatabase()

        // Instantiate DAO
        val ctor = clazz.primaryConstructor
            ?: error("${clazz.simpleName} must have a primary constructor(DatabaseService)")

        val instance = ctor.call(db)

        // Create/update table schema
        transaction(db.db) {
            SchemaUtils.createMissingTablesAndColumns(instance.table)
        }

        // Cache it
        daoCache[clazz] = instance
        return instance
    }

    /**
     * registerSchema: alias for getSchema but returns Unit
     *
     * This is mainly for plugin load-time registration,
     * where you want a single clean call like:
     *
     *     schema.registerSchema(UserDao::class)
     */
    fun registerSchema(clazz: KClass<out BaseDao<*>>) {
        getSchema(clazz)
    }
}
