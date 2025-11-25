package online.veloraplugins.base.paper.plugin

import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.paper.services.command.PaperCommandService
import org.bukkit.plugin.java.JavaPlugin

abstract class PaperBasePlugin : JavaPlugin() {

    lateinit var base: BasePlugin
        private set

    lateinit var paperCommandService: PaperCommandService


    /** De implementatie MOET een BasePlugin instance aanleveren. */
    abstract fun createBasePlugin(): BasePlugin


    override fun onLoad() {
        super.onLoad()

        // BasePlugin ophalen
        this.base = createBasePlugin()

        // Lifecycle
        base.initialize()
        base.onLoad()

        // Paper commands pas na initialize
        paperCommandService = PaperCommandService(this)
    }


    override fun onEnable() {
        super.onEnable()
        base.onEnable()
    }

    override fun onDisable() {
        base.onDisable()
        super.onDisable()
    }
}
