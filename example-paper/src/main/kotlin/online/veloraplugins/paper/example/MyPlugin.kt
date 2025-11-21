package online.veloraplugins.paper.example

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.database.dao.language.LanguageDao
import online.veloraplugins.base.core.database.dao.user.BasicUserDao
import online.veloraplugins.base.core.language.LanguageService
import online.veloraplugins.base.core.redis.RedisService
import online.veloraplugins.base.core.redis.event.RedisEventService
import online.veloraplugins.base.paper.plugin.PaperBasePlugin
import online.veloraplugins.base.paper.services.MaterialsCacheService
import online.veloraplugins.base.paper.services.PlaceholderAPIService
import online.veloraplugins.paper.example.language.ExampleMessage
import online.veloraplugins.paper.example.service.ExampleService

class MyPlugin : PaperBasePlugin() {

    private val base = this.base()

    override fun onLoad() {
        super.onLoad()

        val databaseService = DatabaseService(base)

        databaseService.schemas.register(BasicUserDao::class)
        databaseService.schemas.register(LanguageDao::class)

        val redisService = RedisService(base)

        RedisEventService(base, redisService)

        MaterialsCacheService(this)

        val placeholderAPIService = PlaceholderAPIService(this, "baseplugin")
        placeholderAPIService.register("test") {
                player -> "I am working ${player.name}"
        }

        val languageService = LanguageService(base)
        languageService.registerEnum(McLanguage.EN_US, ExampleMessage::class.java)
        languageService.registerEnum(McLanguage.NL_NL, ExampleMessage::class.java)
        languageService.reloadAll()

        ExampleService(base)
    }

    override fun onEnable() {
        super.onEnable()
    }
}
