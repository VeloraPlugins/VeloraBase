package online.veloraplugins.base.paper.services

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import kotlin.reflect.KClass

/**
 * AbstractPaperService extends [AbstractService] with built-in support for
 * Bukkit/Paper event listeners.
 *
 * Any Paper-specific service that needs to listen for events should inherit
 * from this class.
 *
 * Features:
 *  - Automatic listener registration during onEnable()
 *  - Automatic listener unregistration during onDisable()
 *  - Thread-safe listener storage
 *
 * Services extending this class should call:
 *
 *      registerListener(MyListener())
 *
 * inside their onEnable() implementation.
 */
abstract class AbstractPaperService(
    private val app: PaperBasePlugin
) : AbstractService(app.base()) {

    /** Internal storage for all listeners registered by this service */
    private val listeners = mutableListOf<Listener>()

    /**
     * Registers a Bukkit/Paper event listener for this service.
     *
     * This MUST only be used inside onEnable() since listeners will be automatically
     * unregistered when the service shuts down.
     *
     * @param listener The event listener instance to register.
     */
    protected fun registerListener(listener: Listener) {
        listeners += listener
        Bukkit.getPluginManager().registerEvents(listener, app)
    }

    /**
     * Ensures all listeners registered by this service are unregistered when the
     * service is disabled.
     */
    override suspend fun onDisable() {
        listeners.forEach { HandlerList.unregisterAll(it) }
        listeners.clear()
        super.onDisable()
    }
}
