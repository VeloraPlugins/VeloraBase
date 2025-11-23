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

        // Core
        base.serviceManager.register(DatabaseService(base))
        base.serviceManager.register(DaoService(base))

        // 2) direct DAOs registeren!
        val schema = base.serviceManager.require(DaoService::class)
        schema.register(BasicUserDao::class)

        // Redis + events
        base.serviceManager.register(RedisService(base))
        base.serviceManager.register(RedisEventService(base))

        // Paper services
        base.serviceManager.register(MaterialsCacheService(this))
        base.serviceManager.register(PaperCommandService(this))
        val papi = base.serviceManager.register(PlaceholderAPIService(this, "example"))

        // Languages
        val lang = base.serviceManager.register(LanguageService(base))

        // Custom
        base.serviceManager.register(ExampleService(base))

        // You can already configure minor things
        papi.register("hello") { p -> "Hello ${p.name}" }
        lang.registerEnum(McLanguage.EN_US, ExampleMessage::class.java)
        lang.registerEnum(McLanguage.NL_NL, ExampleMessage::class.java)
    }


    override fun onEnable() {
        super.onEnable()

        val base = base()
        // Reload languages after schemas exist
        base.serviceManager.require(LanguageService::class).reloadAll()
    }
}
