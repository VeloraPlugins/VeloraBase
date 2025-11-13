package online.veloraplugins.base.core.service

import kotlin.reflect.KClass

/**
 * The base interface for all services managed by the plugin's [ServiceManager].
 *
 * A service represents a modular, self-contained unit of functionality
 * that can be started and stopped independently. The lifecycle is fully
 * asynchronous, allowing service initialization to perform I/O, database
 * operations, or coroutine-based tasks without blocking the main thread.
 *
 * Every service:
 * - Has a unique [key] identifying its type
 * - May declare dependencies on other services
 * - Can be enabled and disabled asynchronously
 * - Reports whether it is currently active via [isEnabled]
 *
 * Typical implementations should extend [AbstractService] rather than
 * implementing this interface directly, unless a fully custom lifecycle
 * is required.
 */
interface Service {

    /**
     * Unique identifier for this service type.
     *
     * The value is typically set to the runtime class of the service,
     * and is used by [ServiceManager] for registry indexing and dependency sorting.
     */
    val key: KClass<out Service>

    /**
     * Human-readable name of the service, derived from its class name.
     * This is used primarily for logging and debugging.
     */
    val name: String
        get() = this.key.simpleName ?: "Service"

    /**
     * Set of other service types that must be enabled before this service.
     *
     * Dependency relationships are resolved using a topological sort.
     * Missing dependencies or circular dependency graphs will cause
     * startup failures with descriptive errors.
     *
     * Override this in services that depend on other services.
     */
    val dependsOn: Set<KClass<out Service>>
        get() = emptySet()

    /**
     * Enables the service asynchronously.
     *
     * Called by [ServiceManager] during plugin initialization.
     * Implementations should perform any required startup logic here,
     * such as opening database connections, initializing caches,
     * or registering event listeners.
     *
     * This method is `suspend` to support non-blocking I/O operations.
     */
    suspend fun enable() {}

    /**
     * Disables the service asynchronously.
     *
     * Called by [ServiceManager] during plugin shutdown.
     * Implementations should perform cleanup here, such as closing connections
     * or releasing resources.
     *
     * This method is `suspend` to support non-blocking cleanup.
     */
    suspend fun disable() {}

    /**
     * Indicates whether the service is currently enabled.
     *
     * Service state is managed by [ServiceManager] or [AbstractService].
     */
    val isEnabled: Boolean
}
