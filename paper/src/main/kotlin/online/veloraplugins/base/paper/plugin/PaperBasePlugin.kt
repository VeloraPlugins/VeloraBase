package online.veloraplugins.base.paper.plugin

import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.AbstractConfigService
import online.veloraplugins.base.core.scheduler.SchedulerService
import online.veloraplugins.base.paper.config.PaperConfigService
import online.veloraplugins.base.paper.scheduler.PaperSchedulerService
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.logging.Logger

abstract class PaperBasePlugin : JavaPlugin() {

    private val base = object : BasePlugin() {

        override val scope = this@PaperBasePlugin.scope

        override val logger: Logger
            get() = this@PaperBasePlugin.logger

        override val dataFolder: File
            get() = this@PaperBasePlugin.dataFolder

        override val pluginVersion: String
            get() = this@PaperBasePlugin.description.version

        override fun createConfigService(): AbstractConfigService =
            PaperConfigService(this@PaperBasePlugin)
    }

    override fun onLoad() {
        base.initialize()

        base.serviceManager.registerInstance(PaperSchedulerService(this))

        base.onLoad()
        super.onLoad()
    }


    override fun onEnable() {
        base.onEnable()
        super.onEnable()
    }

    override fun onDisable() {
        base.onDisable()
        super.onDisable()
    }

    fun base(): BasePlugin = base
}
