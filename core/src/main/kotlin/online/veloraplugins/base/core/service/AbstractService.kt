package online.veloraplugins.base.core.service

import online.veloraplugins.base.core.BasePlugin
import kotlin.reflect.KClass

/**
 * AbstractService provides a minimal and convenient base implementation
 * of the [Service] interface, including:
 *
 * - Automatic lifecycle state tracking
 * - Thread-safe `isEnabled` flag
 * - Logging helpers
 * - Template methods for custom enable/disable logic
 *
 * This base class ensures consistent lifecycle handling across all services
 * while allowing subclasses to focus solely on implementing their actual logic.
 *
 * Extend this class for most service implementations.
 *
 * @param app The owning [BasePlugin], used primarily for logging.
 */
abstract class AbstractService(
    private val app: BasePlugin
) : Service {

    /**
     * Unique service key used by [ServiceManager] to identify this service type.
     * Defaults to the runtime class of the concrete service.
     */
    override val key: KClass<out Service> = this::class

    /**
     * Backing field for [isEnabled].
     * Marked as `@Volatile` to guarantee correct behavior in multithreaded contexts.
     */
    @Volatile
    private var _enabled = false

    /**
     * Indicates whether the service is currently enabled.
     */
    override val isEnabled: Boolean
        get() = this._enabled

    /**
     * Enables the service if it is not already enabled.
     *
     * The sequence is:
     * 1. Check current enabled state
     * 2. Call [onEnable] (subclass hook)
     * 3. Set internal enabled flag
     * 4. Log the state change
     *
     * This method is `final` to ensure consistent lifecycle behavior.
     */
    final override suspend fun enable() {
        if (this._enabled) return
        this.onEnable()
        this._enabled = true
        this.log("enabled")
    }

    /**
     * Disables the service if it is currently enabled.
     *
     * The sequence is:
     * 1. Check current enabled state
     * 2. Call [onDisable] (subclass hook)
     * 3. Set internal enabled flag
     * 4. Log the state change
     *
     * This method is `final` to ensure consistent lifecycle behavior.
     */
    final override suspend fun disable() {
        if (!this._enabled) return
        this.onDisable()
        this._enabled = false
        this.log("disabled")
    }

    /**
     * Hook executed when the service is being enabled.
     * Subclasses override this to implement custom startup logic.
     *
     * This method runs *after* dependency services are enabled
     * and *before* the service's state flag is updated.
     */
    protected open suspend fun onEnable() {}

    /**
     * Hook executed when the service is being disabled.
     * Subclasses override this to implement custom shutdown logic.
     *
     * This method runs *before* the service's state flag is updated.
     */
    protected open suspend fun onDisable() {}

    /**
     * Logs a lifecycle message using the plugin's debug system.
     *
     * @param msg The message to log.
     */
    protected fun log(msg: String) {
        this.app.debug("[Service:${this.name}] $msg")
    }
}
