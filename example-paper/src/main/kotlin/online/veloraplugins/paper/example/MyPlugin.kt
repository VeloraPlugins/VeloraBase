package online.veloraplugins.paper.example

import online.veloraplugins.base.common.enums.McLanguage
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
import online.veloraplugins.paper.example.language.ExampleMessage
import online.veloraplugins.paper.example.service.ExampleService

class MyPlugin : PaperBasePlugin() {

    override fun onLoad() {
        super.onLoad()

        val base = base()

        // 1) eerst services registreren
        base.serviceManager.register(DatabaseService(base))
        base.serviceManager.register(DaoService(base))

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

        val schema = base.serviceManager.require(DaoService::class)
        schema.register(BasicUserDao::class)
        schema.register(LanguageDao::class)
        // Reload languages after schemas exist
        base.serviceManager.require(LanguageService::class).reloadAll()
    }
}
