package online.veloraplugins.base.paper.plugin

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.AbstractConfigService
import online.veloraplugins.base.paper.config.PaperConfigService
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Logger

/**
 * Paper-specific implementation of the BasePlugin abstraction.
 *
 * Bridges the VeloraBase core into the Paper/Bukkit ecosystem by providing:
 * - Bukkit logger
 * - Bukkit data folder
 * - Bukkit lifecycle integration
 * - Paper-compatible config service
 * - Automatic service enabling/disabling
 */
abstract class PaperBasePlugin : JavaPlugin() {

    private val base = object : BasePlugin() {

        override val logger: Logger
            get() = this@PaperBasePlugin.logger

        override val dataFolder: File
            get() = this@PaperBasePlugin.dataFolder

        override val pluginVersion: String
            get() = this@PaperBasePlugin.description.version

        /**
         * Paper/Bukkit configurer and serdes setup.
         */
        override fun createConfigService(): AbstractConfigService =
            PaperConfigService(this@PaperBasePlugin)

    }

    override fun onLoad() {
        this.base.initialize()
        this.base.onLoad()
        super.onLoad()
    }

    override fun onEnable() {
        this.base.onEnable()
        super.onEnable()
    }

    override fun onDisable() {
        this.base.onDisable()
        super.onDisable()
    }

    /**
     * Provides direct access to the VeloraBase BasePlugin instance.
     */
    fun base(): BasePlugin = this.base
}
