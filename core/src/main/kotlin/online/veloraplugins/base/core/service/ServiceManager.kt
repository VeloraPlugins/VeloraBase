package online.veloraplugins.base.core.service

import online.veloraplugins.base.core.BasePlugin
import kotlin.reflect.KClass

class ServiceManager(
    private val app: BasePlugin
) {

    companion object {
        @Volatile
        var instance: ServiceManager? = null

        /**
         * Called once by BasePlugin during initialization.
         */
        fun init(manager: ServiceManager) {
            instance = manager
        }

        /**
         * Get a service or null.
         */
        inline fun <reified S : Service> get(): S? =
            instance?.get(S::class)

        /**
         * Get a service or throw.
         */
        inline fun <reified S : Service> require(): S =
            instance?.require(S::class)
                ?: error("ServiceManager not initialized!")

        /**
         * Check if initialized.
         */
        fun isInitialized(): Boolean = instance != null
    }

    private val services = LinkedHashMap<KClass<out Service>, Service>()

    fun <S : Service> register(service: S): S {
        val key = service.key
        require(!services.containsKey(key)) {
            "Service ${key.simpleName} is already registered"
        }
        services[key] = service
        return service
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : Service> get(type: KClass<S>): S? =
        services[type] as? S

    fun <S : Service> require(type: KClass<S>): S =
        get(type) ?: error("Service ${type.simpleName} not registered")

    inline fun <reified S : Service> get(): S? = get(S::class)
    inline fun <reified S : Service> require(): S = require(S::class)

    suspend fun load() {
        for (service in services.values) {
            try {
                loadOnDepends(service)
            } catch (e: Exception) {
                app.logger.severe("Failed to load service ${service.name}: ${e.message}")
            }
        }
    }

    private suspend fun loadOnDepends(service: Service) {
        for (dep in service.dependsOn) {
            val dependency = services[dep]
                ?: error("Missing dependency ${dep.simpleName} for ${service.name}")

            if (!dependency.isLoaded) {
                loadOnDepends(dependency)
            }
        }

        if (!service.isLoaded) {
            service.load()
            app.logger.info("The service ${service.name} has been loaded! Depends on: ${service.dependsOn}")
        }
    }

    suspend fun enable() {
        for (service in services.values) {
            try {
                if (!service.isEnabled) {
                    service.enable()
                    app.logger.info("The service ${service.name} has been enabled!")
                }
            } catch (e: Exception) {
                app.logger.severe("Failed to enable service ${service.name}: ${e.message}")
            }
        }
    }

    suspend fun disable() {
        for (service in services.values.reversed()) {
            try {
                if (service.isEnabled) {
                    service.disable()
                    app.logger.info("The service ${service.name} has been disabled!")
                }
            } catch (e: Exception) {
                app.logger.severe("Failed to disable service ${service.name}: ${e.message}")
            }
        }
    }
}
