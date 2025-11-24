package online.veloraplugins.base.core.service

import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class ServiceManager {

    private val services = ConcurrentHashMap<KClass<out Service>, Service>()

    fun <T : Service> register(service: T): T {
        val key = service::class

        require(!services.containsKey(key)) {
            "Service ${key.simpleName} already registered"
        }

        services[key] = service
        service.initializeInternal()

        return service
    }

    suspend fun <T : Service> enable(service: T): Service {
        val key = service::class

        require(services.containsKey(key)) {
            "Service ${key.simpleName} is not registered!"
        }

        service.onEnable()
        return service
    }

    fun loadAll() = runBlocking {
        resolveOrder().forEach { it.onLoad() }
    }

    fun enableAll() = runBlocking {
        resolveOrder().forEach { it.onEnable() }
    }

    fun disableAll() = runBlocking {
        resolveOrder().asReversed().forEach { it.onDisable() }
    }

    /**
     * Resolve final load order using:
     *  1) dependency order (DFS)
     *  2) loadOrder priority (LOWEST → HIGHEST)
     *  3) circular dependency detection
     */
    private fun resolveOrder(): List<Service> {
        val result = mutableListOf<Service>()
        val visited = mutableSetOf<KClass<out Service>>()
        val visiting = mutableSetOf<KClass<out Service>>() // for cycle detection

        fun visit(clazz: KClass<out Service>) {
            if (clazz in visited) return

            if (clazz in visiting) {
                error("Circular dependency detected at service ${clazz.simpleName}")
            }

            val service = services[clazz]
                ?: error("Service ${clazz.simpleName} referenced but not registered!")

            val info = clazz.annotations.filterIsInstance<ServiceInfo>().first()
            visiting += clazz

            // 1. Resolve dependencies FIRST
            for (dep in info.dependsOn) {
                visit(dep)
            }

            // 2. Add service
            visiting -= clazz
            visited += clazz
            result += service
        }

        // First sort by loadOrder priority
        val baseOrder = services.values.sortedBy {
            it.loadOrder.priority
        }

        // Then resolve dependency order on top
        for (service in baseOrder) {
            visit(service::class)
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Service> require(clazz: KClass<T>): T =
        services[clazz] as? T
            ?: error("Service ${clazz.simpleName} not registered!")
}
