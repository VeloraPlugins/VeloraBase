package online.veloraplugins.paper.example

import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.database.core.SchemaService
import online.veloraplugins.base.core.database.dao.user.BasicUserDao
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.services.MaterialsCacheService
import online.veloraplugins.base.paper.services.PlaceholderAPIService
import online.veloraplugins.paper.example.service.ExampleService

class MyPlugin : PaperBasePlugin() {

    private val base = this.base()

    private lateinit var userDao: BasicUserDao

    override fun onLoad() {
        super.onLoad()

        base.serviceManager.registerInstance(DatabaseService(base))
        base.serviceManager.registerInstance(SchemaService(base))
        base.serviceManager.registerInstance(MaterialsCacheService(this))
        base.serviceManager.registerInstance(PlaceholderAPIService(this, "baseplugin"))
        base.serviceManager.registerInstance(ExampleService(base))
    }

    override fun onEnable() {
        super.onEnable()

        // Prepare DAO
        val schema = base.serviceManager.require(SchemaService::class)
        val db = schema.getDb()

        userDao = BasicUserDao(db)

        // Register schema now that DAO exists
        schema.registerSchema(userDao)

        // Register placeholder
        val papi = base.serviceManager.require(PlaceholderAPIService::class)
        papi.register("test") { player -> "I am working ${player.name}" }
    }
}
