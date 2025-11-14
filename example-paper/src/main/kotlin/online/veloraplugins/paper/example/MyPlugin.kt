package online.veloraplugins.paper.example

import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.services.PlaceholderAPIService
import online.veloraplugins.paper.example.service.ExampleService
import org.bukkit.entity.Player

class MyPlugin : PaperBasePlugin() {

    override fun onEnable() {
        super.onEnable()
        val base = this.base()
        base.serviceManager.registerInstance(ExampleService(base))
        base.serviceManager.registerInstance(PlaceholderAPIService(base, "baseplugin")).register("") { player -> "I am working ${player.name}" }
    }
}