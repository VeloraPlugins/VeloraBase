package online.veloraplugins.paper.example

import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.database.core.DaoService
import online.veloraplugins.base.core.database.dao.language.LanguageDao
import online.veloraplugins.base.core.database.dao.user.BasicUserDao
import online.veloraplugins.base.core.language.LanguageService
import online.veloraplugins.base.core.redis.RedisService
import online.veloraplugins.base.core.redis.event.RedisEventService
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.services.MaterialsCacheService
import online.veloraplugins.base.paper.services.PlaceholderAPIService
import online.veloraplugins.base.paper.services.command.PaperCommandService
import online.veloraplugins.paper.example.service.ExampleService

/*class MyPlugin : PaperBasePlugin() {

    override fun registerServices() {
        super.registerServices()

        val base = base()

        base.serviceManager.register(DatabaseService(base))
        val daoService = base.serviceManager.register(DaoService(base))
        daoService.register(BasicUserDao::class)
        daoService.register(LanguageDao::class)

        // 3) andere services
        base.serviceManager.register(RedisService(base))
        base.serviceManager.register(RedisEventService(base))
        base.serviceManager.register(MaterialsCacheService(this))
        base.serviceManager.register(PaperCommandService(this))
        base.serviceManager.register(PlaceholderAPIService(this, "example"))
        base.serviceManager.register(LanguageService(base))
        base.serviceManager.register(ExampleService(base))

    }

    override fun onEnable() {
        super.onEnable()

        val base = base()

        // Reload languages after schemas exist
        base.serviceManager.require(LanguageService::class).reloadAll()
    }
}*/
