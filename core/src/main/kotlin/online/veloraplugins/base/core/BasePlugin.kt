package online.veloraplugins.base.core

import kotlinx.coroutines.*
import online.veloraplugins.base.core.configuration.AbstractConfigService
import online.veloraplugins.base.core.configuration.BaseConfig
import online.veloraplugins.base.core.scheduler.SchedulerService
import online.veloraplugins.base.core.service.ServiceManager
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Core platform-agnostic plugin foundation for the VeloraBase framework.
 *
 * BasePlugin provides:
 * - A unified lifecycle (initialize, onLoad, onEnable, onDisable)
 * - Automatic creation of the plugin data directory
 * - Service management (via ServiceManager)
 * - Coroutine scope tied to the plugin lifecycle
 * - Auto-loaded BaseConfig containing common settings (e.g., debugging)
 *
 * Platform-specific plugins (e.g., Paper, Velocity) should extend their
 * respective platform adapters (PaperBasePlugin, VelocityBasePlugin), not this class directly.
 */
abstract class BasePlugin {

    /**
     * Provides access to the global scheduler service.
     *
     * Lazy-loaded so it becomes available only after initialize() has
     * registered the service via registerCoreServices().
     */
    val scheduler: SchedulerService by lazy {
        serviceManager.require<SchedulerService>()
    }

    /**
     * Central service registry and lifecycle controller.
     *
     * All services must be registered before onEnable().
     */
    lateinit var serviceManager: ServiceManager
        private set

    /**
     * Config service responsible for loading and binding Okaeri config files.
     * Each platform provides its own implementation.
     */
    lateinit var configService: AbstractConfigService
        private set

    /**
     * The global configuration available to every plugin.
     * Automatically created when BasePlugin initializes.
     */
    lateinit var pluginConfig: BaseConfig
        private set

    /**
     * CoroutineScope tied to the plugin lifecycle.
     * Cancelled during onDisable().
     */
    open val scope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * Initializes the VeloraBase context.
     * Should be called by the platform adapter during onLoad().
     *
     * Performs:
     * - Data folder creation
     * - ServiceManager & ConfigService setup
     * - BaseConfig loading
     */
    fun initialize() {
        this.initDataFolder()
        this.initServices()
        this.registerCoreServices()
        this.initBaseConfig()
    }

    /**
     * Ensures the plugin data directory exists.
     */
    private fun initDataFolder() {
        try {
            if (!this.dataFolder.exists()) {
                this.dataFolder.mkdirs()
            }
        } catch (ex: Exception) {
            this.logger.log(Level.SEVERE, "Unable to create plugin directory: ${this.dataFolder.path}", ex)
        }
    }

    /**
     * Creates the ServiceManager and ConfigService.
     * Implemented by each platform adapter.
     */
    private fun initServices() {
        this.serviceManager = ServiceManager(this)
        this.configService = this.createConfigService()
    }

    /**
     * Loads the shared base-settings.yml configuration.
     */
    private fun initBaseConfig() {
        this.pluginConfig = this.configService.create(
            BaseConfig::class,
            "base-settings.yml"
        )
    }

    private fun registerCoreServices() {
        this.serviceManager.registerInstance(SchedulerService(this))
    }

    /**
     * Called before the plugin fully enables.
     * Platform adapters should call super.onLoad().
     */
    open fun onLoad() {
        this.logger.info("Loading BasePlugin...")
    }

    /**
     * Enables the plugin and all registered services.
     */
    open fun onEnable() {
        this.logger.info("Enabling BasePlugin...")
        this.serviceManager.enableAllBlocking()
    }

    /**
     * Disables the plugin and shuts down services and coroutines.
     */
    open fun onDisable() {
        this.logger.info("Disabling BasePlugin...")
        this.serviceManager.disableAllBlocking()
        this.scope.cancel()
    }

    /**
     * Emits a debug log message if debug mode is enabled.
     */
    open fun debug(message: String, vararg args: Any) {
        if (this.isDebugEnabled()) {
            this.logger.info("[DEBUG] " + message.format(*args))
        }
    }

    /**
     * Determines whether debug mode is active, based on the BaseConfig.
     */
    open fun isDebugEnabled(): Boolean =
        this.pluginConfig.debug

    // Abstract API surface required by platform adapters:

    /** Logging adapter for the current platform. */
    abstract val logger: Logger

    /** Data folder for config storage. */
    abstract val dataFolder: File

    /** Plugin version as defined by the platform. */
    abstract val pluginVersion: String

    /** Creates the platform-specific config service. */
    abstract fun createConfigService(): AbstractConfigService
}
