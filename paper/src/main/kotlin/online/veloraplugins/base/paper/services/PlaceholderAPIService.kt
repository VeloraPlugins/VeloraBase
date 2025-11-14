package online.veloraplugins.base.paper.services

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import online.veloraplugins.base.core.scheduler.SchedulerService
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class PlaceholderAPIService(
    private val app: PaperBasePlugin,
    private val namespace: String
) : AbstractService(app.base()) {

    private val placeholders = ConcurrentHashMap<String, suspend (Player) -> String>()

    private var expansion: Expansion? = null

    override val dependsOn = setOf<KClass<out Service>>(SchedulerService::class)

    override suspend fun onEnable() {
        Bukkit.getScheduler().runTask(app, Runnable {
            expansion = Expansion(namespace, placeholders).also {
                it.register()
            }
            log("Registered PlaceholderAPI namespace: $namespace")
        })
    }
    override suspend fun onDisable() {
        expansion?.unregister()
        expansion = null
        placeholders.clear()
        log("Unregistered PlaceholderAPI namespace: $namespace")
    }

    /**
     * Registers a standard synchronous placeholder.
     *
     * Usage:
     *    register("balance") { player -> "100" }
     */
    fun register(id: String, handler: (Player) -> String) {
        placeholders[id.lowercase()] = { player -> handler(player) }
    }

    /**
     * Registers an asynchronous placeholder.
     *
     * Usage:
     *    registerAsync("coins") { player -> db.getCoins(player) }
     */
    fun registerAsync(id: String, handler: suspend (Player) -> String) {
        placeholders[id.lowercase()] = handler
    }

    /**
     * Internal PlaceholderExpansion wrapper.
     */
    private class Expansion(
        private val namespace: String,
        private val map: ConcurrentHashMap<String, suspend (Player) -> String>
    ) : PlaceholderExpansion() {

        override fun getIdentifier(): String = namespace.lowercase()
        override fun getAuthor(): String = "VeloraBase"
        override fun getVersion(): String = "1.0.0"

        override fun onPlaceholderRequest(player: Player?, params: String): String? {
            if (player == null) return ""

            val key = params.lowercase()
            val handler = map[key] ?: return null

            return try {
                kotlinx.coroutines.runBlocking {
                    handler(player)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                "ERR"
            }
        }
    }
}
