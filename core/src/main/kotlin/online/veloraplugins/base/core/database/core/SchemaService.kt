package online.veloraplugins.base.core.database.core

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.dao.BaseDao
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@ServiceInfo("Schema")
class SchemaService(
    private val app: BasePlugin,
    private val databaseService: DatabaseService
) : Service(app) {

    /** Cache: DAO class → DAO instance */
    private val daoCache = mutableMapOf<KClass<out BaseDao<*>>, BaseDao<*>>()

    /**
     * Registers a DAO instance using reflection.
     *
     * BaseDao.init() (schema creation) happens later in onEnable().
     */
    fun <T : BaseDao<*>> register(clazz: KClass<T>): T {

        val ctor = clazz.primaryConstructor
            ?: error("${clazz.simpleName} must have a primary constructor(DatabaseService)")

        app.debug("[SchemaService] Creating DAO instance for ${clazz.simpleName}")

        val instance = ctor.call(databaseService)

        daoCache[clazz] = instance

        app.debug("[SchemaService] Registered DAO ${clazz.simpleName}")

        return instance
    }

    /**
     * Retrieve a previously registered DAO.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseDao<*>> get(type: KClass<T>): T =
        daoCache[type] as? T
            ?: error("DAO ${type.simpleName} is not registered")

    inline fun <reified T : BaseDao<*>> get(): T =
        get(T::class)

    /**
     * Called during plugin enable.
     *
     * Runs BaseDao.init() for all DAOs — which creates or updates schema.
     */
    override suspend fun onEnable() {
        app.debug("[SchemaService] Initializing all registered DAOs...")

        for (dao in daoCache.values) {
            try {
                app.debug("[SchemaService] Initializing DAO ${dao::class.simpleName}")
                dao.init()
                app.debug("[SchemaService] Initialized DAO ${dao::class.simpleName}")
            } catch (e: Exception) {
                app.logger.severe("Failed to initialize DAO ${dao::class.simpleName}: ${e.message}")
            }
        }

        super.onEnable()
    }
}
