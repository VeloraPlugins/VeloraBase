package online.veloraplugins.base.core.configuration

import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.configurer.Configurer
import eu.okaeri.configs.serdes.OkaeriSerdesPack
import java.io.File
import kotlin.reflect.KClass

/**
 * AbstractConfigService provides a unified, reusable configuration system
 * based on Okaeri Configs. It handles creation, loading, registration,
 * tracking, and reloading of configuration files.
 *
 * Features:
 * - Automatic file binding and default saving
 * - Optional custom configurer and serdes support
 * - In-memory registry of loaded configs
 * - Type-safe config retrieval
 * - Bulk reload functionality
 *
 * Typical usage:
 * - A platform-specific plugin extends BasePlugin
 * - BasePlugin creates this service once during initialization
 * - Modules request and use configuration files through this service
 *
 * This class is intended to be subclassed when platform-specific behavior
 * (e.g., logging or file placement logic) must be added.
 *
 * @param dataFolder The base folder where configuration files will be stored.
 * @param configurer The Okaeri Configurer used to define file format and behavior.
 * @param serdes Optional list of additional serialization packs.
 */
open class AbstractConfigService(
    private val dataFolder: File,
    private val configurer: Configurer,
    private val serdes: List<OkaeriSerdesPack> = emptyList()
) {

    /**
     * Internal structure storing all registered configuration instances,
     * their associated class, and their physical storage file.
     */
    private data class RegisteredConfig(
        val cls: KClass<out OkaeriConfig>,
        val instance: OkaeriConfig,
        val file: File
    )

    /** List of all registered configurations managed by this service. */
    private val configs = mutableListOf<RegisteredConfig>()

    /**
     * Creates (or loads, if it already exists) a configuration file of the given type.
     *
     * The file will be created inside the data folder under the specified name.
     * Defaults are saved automatically if the file does not exist.
     *
     * @param configClass The OkaeriConfig class to instantiate.
     * @param name The filename to bind (e.g., "settings.yml").
     * @return The loaded configuration instance.
     */
    fun <T : OkaeriConfig> create(configClass: KClass<T>, name: String): T {
        val file = File(dataFolder, name)

        val config = ConfigManager.create(configClass.java) {
            it.withConfigurer(configurer, *serdes.toTypedArray())
                .withBindFile(file)
                .withRemoveOrphans(true)
                .saveDefaults()
                .load(true)
        }

        configs += RegisteredConfig(configClass, config, file)
        return config
    }

    /**
     * Reified shorthand version of [create].
     *
     * @param name The filename to create/load.
     * @return The loaded configuration instance.
     */
    inline fun <reified T : OkaeriConfig> create(name: String): T =
        create(T::class, name)

    /**
     * Reloads all registered configuration files.
     *
     * Any errors that occur during reload are printed to the console,
     * but do not interrupt the reloading of other configs.
     */
    fun reloadAll() {
        configs.forEach {
            try {
                it.instance.load(true)
            } catch (ex: Exception) {
                println("Failed to reload ${it.file.name}: ${ex.message}")
            }
        }
    }

    /**
     * Retrieves an existing config if it has already been created,
     * otherwise creates a new one.
     *
     * @param configClass The config type.
     * @param name The filename associated with the config.
     * @return The existing or newly created config instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : OkaeriConfig> getConfig(configClass: KClass<T>, name: String): T =
        configs.firstOrNull { it.cls == configClass && it.file.name == name }
            ?.instance as? T ?: create(configClass, name)
}
