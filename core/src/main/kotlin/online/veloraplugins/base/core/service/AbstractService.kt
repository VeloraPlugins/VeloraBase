package online.veloraplugins.base.core.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import online.veloraplugins.base.core.BasePlugin
import kotlin.reflect.KClass

/**
 * AbstractService provides a minimal and consistent base implementation
 * for all services in the plugin. It supports:
 *
 * - onLoad()
 * - onEnable()
 * - onDisable()
 *
 * Services extending this class should override these methods as needed.
 */
abstract class AbstractService(
    private val app: BasePlugin
) : Service {

    init {
        runBlocking { onInit() }
    }

    /**
     * Unique key for this service.
     * By default this is the concrete class type.
     */
    override val key: KClass<out Service> = this::class

    @Volatile
    private var _enabled = false

    @Volatile
    private var _loaded = false

    /** Whether the service is enabled */
    override val isEnabled: Boolean
        get() = _enabled

    /** Whether the service has been loaded */
    override val isLoaded: Boolean
        get() = _loaded

    final override suspend fun load() {
        if (_loaded) return

        onLoad()
        _loaded = true
        log("loaded")
    }

    final override suspend fun enable() {
        if (_enabled) return

        onEnable()
        _enabled = true
        log("enabled")
    }

    final override suspend fun disable() {
        if (!_enabled) return

        onDisable()
        _enabled = false
        log("disabled")
    }

    protected open suspend fun onInit() {}
    protected open suspend fun onLoad() {}
    protected open suspend fun onEnable() {}
    protected open suspend fun onDisable() {}

    // Logging
    protected fun log(msg: String) {
        app.info("[Service:${key.simpleName}] $msg")
    }

    // Logging
    protected fun debug(msg: String) {
        app.debug("[Service:${key.simpleName}] $msg")
    }
}
