package online.veloraplugins.base.paper.example

import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.example.service.ExampleService

class MyPlugin : PaperBasePlugin() {

    override fun onEnable() {
        super.onEnable()
        val base = this.base()
        base.serviceManager.registerInstance(ExampleService(base))
    }
}