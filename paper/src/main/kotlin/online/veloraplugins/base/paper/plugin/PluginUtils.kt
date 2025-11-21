package online.veloraplugins.base.paper.plugin

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

@Suppress("unused")
object PluginUtils {
    fun <T> getRegistration(clazz: Class<T>): T? {
        return Bukkit.getServicesManager().load(clazz)
    }

    fun <T> getPluginRegistrar(clazz: Class<T>): T? {
        return Bukkit.getServicesManager().getRegistration(clazz)?.provider
    }

    fun getPlugin(name: String): Plugin? {
        return Bukkit.getPluginManager().getPlugin(name)
    }

    fun <T : Plugin> getPlugin(name: String, clazz: Class<T>): T? {
        val plugin = Bukkit.getPluginManager().getPlugin(name)

        if (plugin == null || !clazz.isAssignableFrom(plugin.javaClass)) return null

        return clazz.cast(plugin)
    }

    fun hasPlugin(plugin: String): Boolean {
        return Bukkit.getPluginManager().getPlugin(plugin) != null
    }

    fun isEnabled(plugin: String): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled(plugin)
    }

    fun shutdownServer() {
        Bukkit.getServer().shutdown()
    }
}
