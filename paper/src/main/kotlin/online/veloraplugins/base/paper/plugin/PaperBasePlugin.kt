package online.veloraplugins.base.paper.plugin

import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.CoroutineScope
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.configuration.AbstractConfigService
import online.veloraplugins.base.paper.config.PaperConfigService
import online.veloraplugins.base.paper.services.command.PaperCommandService
import online.veloraplugins.mccommon.ComponentUtil
import org.bukkit.plugin.java.JavaPlugin

abstract class PaperBasePlugin : JavaPlugin() {

    private val base = object : BasePlugin() {

        override val platformScope: CoroutineScope by lazy {
            this@PaperBasePlugin.scope
        }

        override fun info(message: String) {
            componentLogger.info(ComponentUtil.parse(message))
        }

        override fun createConfigService(): AbstractConfigService =
            PaperConfigService(this@PaperBasePlugin)

        override val logger get() = this@PaperBasePlugin.logger
        override val dataFolder get() = this@PaperBasePlugin.dataFolder
        override val pluginVersion get() = this@PaperBasePlugin.description.version
    }


    override fun onLoad() {
        this.base.initialize()
        this.base.onLoad()
        this.base.serviceManager.register(PaperCommandService(this))
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

    fun base(): BasePlugin = base
}
