package online.veloraplugins.base.paper.services

import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/**
 * AbstractPaperService integrates AbstractService with Bukkit/Paper's event system.
 *
 * - Registers itself as a listener by default
 * - Supports suspending event listeners (MCCoroutine)
 * - Automatically unregisters listeners on disable
 */
abstract class PaperService(
    private val app: PaperBasePlugin
) : Service(app.base), Listener {

    private val listeners = mutableListOf<Listener>()

    /**
     * Enable lifecycle — automatically registers service as event listener.
     */
    override suspend fun onEnable() {
        super.onEnable()
        registerSelf()
    }

    /**
     * Registers an event listener and tracks it for cleanup.
     */
    protected fun registerListener(listener: Listener) {
        listeners += listener
        Bukkit.getPluginManager().registerSuspendingEvents(listener, app)
        debug("Registered listener: ${listener::class.simpleName}")
    }

    /**
     * Unregister all listeners on disable.
     */
    override suspend fun onDisable() {
        listeners.forEach { HandlerList.unregisterAll(it) }
        listeners.clear()
        super.onDisable()
    }

    /**
     * Registers this service instance as a listener.
     */
    private fun registerSelf() {
        registerListener(this)
    }
}
