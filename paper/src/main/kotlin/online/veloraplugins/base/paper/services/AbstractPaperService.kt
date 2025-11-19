package online.veloraplugins.base.paper.services

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/**
 * AbstractPaperService extends [AbstractService] with built-in support for
 * Bukkit/Paper event listener management.
 *
 * ## Features
 * - Optional automatic registration of the service itself as a listener
 * - Ability to register arbitrary additional listeners
 * - Automatic unregistration on disable
 *
 * ## Auto Self-Registration
 * By default, the service registers itself (`this`) as a listener during [onEnable].
 *
 * To disable this behavior:
 *
 * ```
 * override suspend fun onEnable() {
 *     super.onEnable(autoRegisterSelf = false)
 *     registerListener(MyListener())
 * }
 * ```
 *
 * @param app The [PaperBasePlugin] instance to register listeners with.
 */
abstract class AbstractPaperService(
    private val app: PaperBasePlugin
) : AbstractService(app.base()), Listener {

    /** Internal storage for listeners registered by this service. */
    private val listeners = mutableListOf<Listener>()

    /**
     * Lifecycle hook called when the service is enabled.
     *
     * @param autoRegisterSelf Whether this service should automatically register itself
     * as a Bukkit event listener. Default is `true`.
     */
    override suspend fun onEnable() {
        registerSelf()
    }

    /**
     * Registers a Bukkit/Paper listener and tracks it internally for cleanup.
     *
     * @param listener The listener to register.
     */
    protected fun registerListener(listener: Listener) {
        listeners += listener
        Bukkit.getPluginManager().registerSuspendingEvents(listener, app)
    }

    /**
     * Unregisters all listeners registered by this service.
     */
    override suspend fun onDisable() {
        listeners.forEach { HandlerList.unregisterAll(it) }
        listeners.clear()
        super.onDisable()
    }

    /**
     * Registers this service instance as a listener.
     */
    protected fun registerSelf() {
        app.base().debug("Registered ${this.name} as bukkit Listener!")
        registerListener(this)
    }
}
