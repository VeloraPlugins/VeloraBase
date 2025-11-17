package online.veloraplugins.base.core.service

import kotlin.reflect.KClass

/**
 * The base interface for all services managed by the plugin's [ServiceManager].
 *
 * A service represents a modular, self-contained unit of functionality
 * that can be loaded, enabled, and disabled independently.
 *
 * Lifecycle (all asynchronous):
 * - load()    → construct heavy objects (database, caches, configs)
 * - enable()  → activate listeners, commands, schedulers
 * - disable() → clean up resources
 *
 * Services should normally extend [AbstractService], which implements
 * all lifecycle state tracking automatically.
 */
interface Service {

    /** Unique identifier for this service type. */
    val key: KClass<out Service>

    /** Human-readable service name. */
    val name: String
        get() = key.simpleName ?: "Service"

    /**
     * Declares dependencies that must be loaded and enabled first.
     */
    val dependsOn: Set<KClass<out Service>>
        get() = emptySet()

    /**
     * Called once before enable(), used to prepare heavy resources.
     */
    suspend fun load() {}

    /**
     * Activates the service (register listeners, start schedulers).
     */
    suspend fun enable() {}

    /**
     * Deactivates the service (cleanup, unregister listeners).
     */
    suspend fun disable() {}

    /** Whether the service has been loaded. */
    val isLoaded: Boolean
        get() = false

    /** Whether the service is active. */
    val isEnabled: Boolean
}
