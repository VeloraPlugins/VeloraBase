package online.veloraplugins.base.core.database.core

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.dao.BaseDao
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.core.service.Service
import kotlin.reflect.KClass

/**
 * SchemaService manages all database table creation and safe schema updates.
 *
 * Plugins and services register their DAOs, and when SchemaService enables,
 * it automatically runs Exposed's createMissingTablesAndColumns() on each table.
 *
 * Ensures:
 *  - Tables always exist
 *  - New columns get added safely
 *  - No destructive modifications
 *  - Consistent startup order
 */
class SchemaService(
    private val app: BasePlugin,
) : AbstractService(app) {

    override val dependsOn: Set<KClass<out Service>> =
        setOf(DatabaseService::class)

    /** All registered DAOs whose schemas must be managed */
    private val registeredDaos = mutableListOf<BaseDao<*>>()

    /**
     * Returns the active DatabaseService instance.
     *
     * This is always safe to call because SchemaService depends on DatabaseService,
     * ensuring it is initialized first.
     */
    fun getDatabase(): DatabaseService =
        app.serviceManager.get(DatabaseService::class)
            ?: error("DatabaseService is not loaded yet!")

    /** Optional shorter alias */
    fun getDb(): DatabaseService = getDatabase()

    /**
     * Registers a DAO so that its schema is updated automatically.
     *
     * Should be called by modules/services during setup.
     */
    fun registerSchema(dao: BaseDao<*>) {
        registeredDaos += dao
    }

    /**
     * Registers multiple DAOs at once.
     */
    fun registerSchemas(vararg daos: BaseDao<*>) {
        registeredDaos += daos
    }

    override suspend fun onEnable() {
        super.onEnable()
        applyUpdates()
    }


    /**
     * Performs schema updates for all registered DAOs.
     *
     * Must be called manually by a plugin or service.
     */
    private fun applyUpdates() {
        app.scheduler.run {
            log("Starting schema updates for ${registeredDaos.size} tables...")

            for (dao in registeredDaos) {
                try {
                    dao.updateSchema()
                    log("✔ Updated schema for table: ${dao.table.tableName}")
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    log("✘ Failed to update schema for ${dao.table.tableName}: ${ex.message}")
                }
            }

            log("Schema update complete.")
        }
    }
}
