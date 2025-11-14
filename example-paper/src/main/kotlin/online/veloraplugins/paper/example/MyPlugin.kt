package online.veloraplugins.paper.example

import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.services.MaterialsCacheService
import online.veloraplugins.base.paper.services.PlaceholderAPIService
import online.veloraplugins.paper.example.service.ExampleService
import org.bukkit.entity.Player

class MyPlugin : PaperBasePlugin() {

    private val base = this.base()

    override fun onLoad() {
        super.onLoad()
        base.serviceManager.registerInstance(ExampleService(base))
        base.serviceManager.registerInstance(MaterialsCacheService(this))
        base.serviceManager.registerInstance(PlaceholderAPIService(this, "baseplugin")).register("") { player -> "I am working ${player.name}" }
    }
}