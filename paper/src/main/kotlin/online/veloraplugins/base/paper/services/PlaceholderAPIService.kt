package online.veloraplugins.base.paper.services

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.core.service.Service
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class PlaceholderAPIService(
    private val app: BasePlugin,
    private val namespace: String
) : AbstractService(app) {

    private val placeholders = ConcurrentHashMap<String, suspend (Player) -> String>()

    private var expansion: Expansion? = null

    override val dependsOn: Set<KClass<out Service>> = emptySet()

    override suspend fun onEnable() {
        expansion = Expansion(namespace, placeholders).also {
            it.register()
        }
        log("Registered PlaceholderAPI namespace: $namespace")
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
