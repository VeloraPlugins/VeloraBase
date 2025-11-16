package online.veloraplugins.base.core.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import online.veloraplugins.base.core.BasePlugin
import kotlin.reflect.KClass

/**
 * ServiceManager is responsible for registering, organizing,
 * and controlling the lifecycle of all plugin services.
 *
 * Features:
 * - Stores all services by type
 * - Resolves dependencies using topological sorting
 * - Enables/disables services in the correct order
 * - Supports asynchronous service loading with `suspend`
 * - Provides blocking wrappers for synchronous plugin environments
 *
 * This manager ensures that services are always started in a safe,
 * deterministic, and dependency-aware sequence.
 *
 * @param app The owning plugin instance used for logging and context.
 */
class ServiceManager(
    private val app: BasePlugin
) {

    /**
     * Internal registry of services, mapped by their service type.
     * Each service is uniquely indexed by its KClass key.
     */
    private val services = LinkedHashMap<KClass<out Service>, Service>()

    /**
     * Registers an already constructed service instance.
     *
     * Ensures that only one instance of each service type is registered.
     *
     * @param service The service instance to register.
     * @return The same service instance.
     * @throws IllegalArgumentException If the service type is already registered.
     */
    fun <S : Service> registerInstance(service: S): S {
        val key = service.key
        require(!this.services.containsKey(key)) {
            "Service ${key.simpleName} is already registered"
        }
        this.services[key] = service
        return service
    }

    fun <T : Service> registerInstance(type: KClass<out Service>, service: T) {
        require(!services.containsKey(type)) {
            "Service ${type.simpleName} is already registered"
        }
        services[type] = service
    }

    /**
     * Registers a service using a factory function.
     *
     * This is useful when a service requires access to other
     * services via the ServiceManager during construction.
     *
     * @param factory A function that receives the ServiceManager and returns a service instance.
     * @return The created and registered service.
     */
    fun <S : Service> registerFactory(factory: (ServiceManager) -> S): S {
        val service = factory(this)
        return this.registerInstance(service)
    }

    /**
     * Retrieves a service by its type, or returns null if it is not registered.
     *
     * @param type The class type of the requested service.
     * @return The service instance, or null if not found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <S : Service> get(type: KClass<S>): S? =
        this.services[type] as? S

    /**
     * Retrieves a service by its type, throwing an error if not found.
     *
     * @param type The class type of the requested service.
     * @return The service instance.
     * @throws IllegalStateException If the service is not registered.
     */
    fun <S : Service> require(type: KClass<S>): S =
        this.get(type) ?: error("Service ${type.simpleName} not registered")

    /** Reified convenience wrapper for [get]. */
    inline fun <reified S : Service> get(): S? = this.get(S::class)

    /** Reified convenience wrapper for [require]. */
    inline fun <reified S : Service> require(): S = this.require(S::class)

    /**
     * Enables all services asynchronously.
     *
     * Steps:
     * 1. Determines the correct load order using dependency resolution.
     * 2. Ensures all required dependencies are enabled first.
     * 3. Calls enable() on each service.
     *
     * Failures are logged but do not interrupt the startup of other services.
     */
    suspend fun enableAll() {
        val order = this.topologicalOrder()

        for (type in order) {
            val service = this.services[type] ?: continue

            try {
                // Enable dependencies first
                for (dep in service.dependsOn) {
                    val depService = this.services[dep]
                        ?: error("Missing dependency ${dep.simpleName} for ${service.name}")

                    if (!depService.isEnabled) {
                        depService.enable()
                    }
                }

                // Enable the service itself
                if (!service.isEnabled) {
                    service.enable()
                }
            } catch (ex: Exception) {
                this.app.logger.severe(
                    "Failed to enable service ${service.name}: ${ex.javaClass.simpleName}: ${ex.message}"
                )
            }
        }
    }

    /**
     * Disables all registered services asynchronously in reverse dependency order.
     *
     * Each disable() call is wrapped in a try/catch block to ensure that
     * all services attempt to shut down cleanly, even if one fails.
     */
    suspend fun disableAll() {
        val order = this.topologicalOrder().asReversed()

        for (type in order) {
            val service = this.services[type] ?: continue

            try {
                if (service.isEnabled) {
                    service.disable()
                }
            } catch (ex: Exception) {
                this.app.logger.severe(
                    "Failed to disable service ${service.name}: ${ex.javaClass.simpleName}: ${ex.message}"
                )
            }
        }
    }

    fun enableServiceBlocking(type: KClass<out Service>) {
        runBlocking(Dispatchers.Default) {
            enableService(type)
        }
    }

    suspend fun enableService(type: KClass<out Service>) {
        val service = this.services[type]
            ?: error("Service ${type.simpleName} is not registered")

        // Enable dependencies first
        for (dep in service.dependsOn) {
            val depService = this.services[dep]
                ?: error("Missing dependency ${dep.simpleName} for ${service.name}")

            if (!depService.isEnabled) {
                enableService(dep)   // recursive enabling
            }
        }

        // Enable this service if not enabled
        if (!service.isEnabled) {
            service.enable()
        }
    }

    /**
     * Performs a topological sort of all registered services based on
     * their declared dependencies.
     *
     * This ensures that:
     * - Dependencies are always enabled before dependents.
     * - Services are disabled in the reverse order.
     * - Cycles in the dependency graph are detected and reported.
     *
     * @return A list of service types in correct dependency order.
     * @throws IllegalStateException If a dependency cycle is detected.
     */
    private fun topologicalOrder(): List<KClass<out Service>> {
        val nodes = this.services.keys.toMutableSet()
        val inDegree = HashMap<KClass<out Service>, Int>()
        val graph = HashMap<KClass<out Service>, MutableSet<KClass<out Service>>>()

        // Initialize graph nodes
        for (n in nodes) {
            inDegree[n] = 0
            graph[n] = mutableSetOf()
        }

        // Build the dependency graph: dep -> dependent service
        for ((_, s) in this.services) {
            for (dep in s.dependsOn) {
                if (!this.services.containsKey(dep)) continue
                graph[dep]!!.add(s.key)
                inDegree[s.key] = (inDegree[s.key] ?: 0) + 1
            }
        }

        // Kahn's algorithm
        val q = ArrayDeque<KClass<out Service>>()
        inDegree.filter { it.value == 0 }.keys.forEach { q.add(it) }

        val out = ArrayList<KClass<out Service>>(this.services.size)
        while (q.isNotEmpty()) {
            val u = q.removeFirst()
            out.add(u)
            for (v in graph[u].orEmpty()) {
                inDegree[v] = (inDegree[v] ?: 0) - 1
                if (inDegree[v] == 0) q.add(v)
            }
        }

        // Detect dependency cycles
        if (out.size != this.services.size) {
            val remaining = this.services.keys - out.toSet()
            val names = remaining.joinToString { it.simpleName ?: "Unknown" }
            error("Service dependency cycle detected: $names")
        }

        return out
    }
}
