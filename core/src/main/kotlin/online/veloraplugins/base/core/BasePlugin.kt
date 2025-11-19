package online.veloraplugins.base.core

import kotlinx.coroutines.*
import online.veloraplugins.base.core.configuration.AbstractConfigService
import online.veloraplugins.base.core.configuration.BaseConfig
import online.veloraplugins.base.core.scheduler.SchedulerService
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceManager
import java.io.File
import java.util.logging.Logger

/**
 * Core platform-agnostic plugin foundation for the VeloraBase framework.
 *
 * BasePlugin provides:
 * - A unified lifecycle (initialize, onLoad, onEnable, onDisable)
 * - Automatic creation of the plugin data directory
 * - Service management (via ServiceManager)
 * - Coroutine scope tied to the plugin lifecycle (platform-provided or fallback)
 * - Auto-loaded BaseConfig containing common settings (e.g., debugging)
 *
 * Platform-specific plugins (e.g., Paper, Velocity) should extend their
 * respective adapters (PaperBasePlugin, VelocityBasePlugin), not this class directly.
 */
abstract class BasePlugin {

    /**
     * Provides access to the global scheduler service.
     *
     * Lazy-loaded so it becomes available only after initialize() has
     * registered the SchedulerService instance.
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
     * Responsible for loading and binding Okaeri config files.
     * Each platform provides its own implementation.
     */
    lateinit var configService: AbstractConfigService
        private set

    /**
     * The global base configuration available to every plugin.
     * Automatically created when BasePlugin initializes.
     */
    lateinit var pluginConfig: BaseConfig
        private set

    /**
     * Optional platform-specific coroutine scope.
     *
     * - Paper should override this to return the MCCoroutine plugin scope.
     * - Velocity usually does not override this.
     */
    open val platformScope: CoroutineScope? = null

    /**
     * Final coroutine scope used everywhere in the system.
     *
     * Resolves to:
     * - The platformScope (if the platform provides its own threading model)
     * - OR a safe fallback SupervisorJob scope (default for non-Paper platforms)
     *
     * This ensures the entire plugin architecture always has a valid coroutine scope.
     */
    val scope: CoroutineScope by lazy {
        platformScope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * Initializes the VeloraBase core before the platform fully loads.
     *
     * Should be called by the platform adapter during onLoad().
     * Performs:
     * - Data folder creation
     * - ServiceManager & ConfigService creation
     * - BaseConfig loading
     * - Registration of core services (SchedulerService)
     */
    fun initialize() {
        initDataFolder()
        initServices()
        initBaseConfig()
        registerCoreServices()
    }

    /**
     * Ensures the plugin data directory exists.
     */
    private fun initDataFolder() {
        if (!dataFolder.exists()) dataFolder.mkdirs()
    }

    /**
     * Creates ServiceManager and configuration service.
     * ConfigService implementation is provided per platform.
     */
    private fun initServices() {
        serviceManager = ServiceManager(this)
        ServiceManager.init(ServiceManager(this))
        configService = createConfigService()
    }

    /**
     * Loads the shared base-settings.yml configuration.
     */
    private fun initBaseConfig() {
        pluginConfig = configService.create(BaseConfig::class, "base-settings.yml")
    }

    /**
     * Registers core VeloraBase services required on every platform.
     * SchedulerService is always required by the infrastructure.
     */
    private fun registerCoreServices() {
        serviceManager.register(SchedulerService(this))
    }

    /**
     * Called before the plugin fully enables.
     *
     * PLEASE NOTE:
     * - No coroutines should be launched here yet.
     * - Paper's MCCoroutine infrastructure is not active until onEnable().
     */
    open fun onLoad() {
        logger.info("Loading BasePlugin...")
    }

    /**
     * Enables the plugin and all registered services.
     *
     * ServiceManager.enableAll() is suspend, but calling blocking here
     * ensures predictable plugin startup without coroutine issues.
     */
    open fun onEnable() {
        logger.info("Enabling BasePlugin...")

        runBlocking {
            serviceManager.load()
            serviceManager.enable()
        }
    }

    /**
     * Disables the plugin, shuts down services, then terminates the main scope.
     */
    open fun onDisable() {
        logger.info("Disabling BasePlugin...")

        runBlocking {
            serviceManager.disable()
        }

        scope.cancel()
    }

    /**
     * Debug logging helper (bound to BaseConfig.debug).
     */
    open fun debug(message: String, vararg args: Any) {
        if (isDebugEnabled()) {
            logger.info("[DEBUG] " + message.format(*args))
        }
    }

    /**
     * logging helper.
     */
    abstract fun info(message: String)

    /**
     * Returns true if debugging is enabled in base-settings.yml.
     */
    open fun isDebugEnabled(): Boolean =
        pluginConfig.debug

    /** Logging adapter for the current platform. */
    abstract val logger: Logger

    /** Data folder for config storage. */
    abstract val dataFolder: File

    /** Plugin version as defined by the platform. */
    abstract val pluginVersion: String

    /** Creates the platform-specific config service. */
    abstract fun createConfigService(): AbstractConfigService
}
