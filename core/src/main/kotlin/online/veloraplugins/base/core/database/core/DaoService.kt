package online.veloraplugins.base.core.database.core

import kotlinx.coroutines.launch
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.dao.BaseDao
import online.veloraplugins.base.core.service.LoadOrder
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * SchemaService
 *
 * - Registreert DAOs (instantie direct, maar init() gebeurt later)
 * - Haalt automatisch DatabaseService op via ServiceManager.require()
 * - Initieert alle DAO schema's tijdens onEnable()
 */
@ServiceInfo(
    name = "DAOs",
    order = LoadOrder.HIGHEST,
    dependsOn = [DatabaseService::class]
)
class DaoService(
    plugin: BasePlugin
) : Service(plugin) {

    /** Alle DAOs die we registreren */
    private val daoCache = mutableMapOf<KClass<out BaseDao<*>>, BaseDao<*>>()

    /** Lazy getter — haalt altijd de bestaande DatabaseService */
    private val database: DatabaseService
        get() = plugin.serviceManager.require(DatabaseService::class)

    /**
     * Registreer een DAO via reflectie.
     *
     * Voorwaarde:
     * - DAO moet primaryConstructor(DatabaseService) hebben.
     */
    fun <T : BaseDao<*>> register(type: KClass<T>): T {

        val ctor = type.primaryConstructor
            ?: error("${type.simpleName} must declare a primary constructor(DatabaseService).")

        plugin.debug("[DaoService] Creating DAO for ${type.simpleName}")

        // Reflectie constructor-call met databaseService
        val dao = ctor.call(database)

        daoCache[type] = dao

        plugin.debug("[DaoService] Registered DAO ${type.simpleName}")

        return dao
    }

    /** Ophalen van DAO */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseDao<*>> get(type: KClass<T>): T =
        daoCache[type] as? T
            ?: error("DAO ${type.simpleName} is not registered!")

    inline fun <reified T : BaseDao<*>> get(): T = get(T::class)

    override suspend fun onEnable() {
        plugin.debug("[DaoService] Initializing all DAOs...")

        daoCache.values.forEach { dao ->
            plugin.debug("Initializing dao = ${dao.javaClass.simpleName}")
            dao.init()
        }

        super.onEnable()
    }
}
