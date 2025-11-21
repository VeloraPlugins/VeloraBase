package online.veloraplugins.base.core.service

import online.veloraplugins.base.core.BasePlugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Base class for modular backend services.
 * Mirrors the design of CMS Module system (auto-init, registry, lifecycle).
 */
abstract class Service(
    open val plugin: BasePlugin
) {

    private lateinit var serviceInfo: ServiceInfo
    private lateinit var loggerName: String

    var loaded: Boolean = false
        private set

    var enabled: Boolean = false
        private set

    init {
        if (!javaClass.isAnnotationPresent(ServiceInfo::class.java)) {
            throw RuntimeException("Missing @ServiceInfo annotation in ${javaClass.name}")
        }

        initialize()
    }

    /** Reads annotation, registers service, calls onInitialize(). */
    private fun initialize() {
        val start = System.currentTimeMillis()

        this.serviceInfo = javaClass.getAnnotation(ServiceInfo::class.java)
        this.loggerName = "Service:${serviceInfo.name}"

        log("Initializing service...")

        // auto-register in global map
        registerService(this)

        onInitialize()

        enabled = true
        log("Service initialized in ${System.currentTimeMillis() - start}ms [v${serviceInfo.version}]")
    }

    /** Runs IMMEDIATELY after constructor (same as Module). */
    protected open fun onInitialize() {}

    /** Called by plugin when loading. */
    open suspend fun onLoad() {
        loaded = true
        log("${name} loaded")
    }

    /** Called by plugin when enabling. */
    open suspend fun onEnable() {
        enabled = true
        log("${name} enabled")
    }

    /** Called by plugin when disabling. */
    open suspend fun onDisable() {
        enabled = false
        log("${name} disabled")
    }

    /** Optional reload hook. */
    open suspend fun onReload() {
        log("${name} reloaded")
    }

    protected fun log(message: String) {
        plugin.info("[$loggerName] $message")
    }

    protected fun debug(message: String) {
        plugin.debug("[$loggerName] $message")
    }

    val name: String
        get() = serviceInfo.name

    companion object {
        /** Global registry of all services */
        private val services = ConcurrentHashMap<KClass<out Service>, Service>()

        /** Register service in global map */
        fun registerService(service: Service) {
            services[service::class] = service
        }

        /** Get existing service */
        @Suppress("UNCHECKED_CAST")
        fun <T : Service> get(clazz: KClass<T>): T? =
            services[clazz] as? T

        /** Get existing service or throw if not found */
        @Suppress("UNCHECKED_CAST")
        fun <T : Service> getOrThrow(clazz: KClass<T>): T =
            services[clazz] as? T
                ?: error("Service ${clazz.simpleName} is not registered!")


        /** Convenience generic getter */
        inline fun <reified T : Service> get(): T =
            get(T::class)
                ?: error("Service '${T::class.simpleName}' not registered!")

        fun all(): List<Service> {
            return services.values.toList()
        }
    }
}