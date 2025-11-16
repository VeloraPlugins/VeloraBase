package online.veloraplugins.paper.example

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.database.core.SchemaService
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

        base.serviceManager.registerInstance(DatabaseService(base))
        base.serviceManager.registerInstance(SchemaService(base))

        base.serviceManager.registerInstance(RedisService(base))
        base.serviceManager.registerInstance(RedisEventService(base))

        base.serviceManager.registerInstance(MaterialsCacheService(this))
        base.serviceManager.registerInstance(PlaceholderAPIService(this, "baseplugin"))

        base.serviceManager.registerInstance(LanguageService(base))
        base.serviceManager.registerInstance(ExampleService(base))
    }

    override fun onEnable() {
        super.onEnable()

        loadTestUserDao()
        loadLanguages()

        val papi = base.serviceManager.require(PlaceholderAPIService::class)
        papi.register("test") { player -> "I am working ${player.name}" }
    }

    private fun loadTestUserDao() {
        // Prepare DAO
        val schema = base.serviceManager.require(SchemaService::class)
        schema.registerSchema(BasicUserDao::class)
    }

    private fun loadLanguages() {
        val languageService = base.serviceManager.require(LanguageService::class)

        languageService.registerEnum(McLanguage.EN_US, ExampleMessage::class.java)
        languageService.registerEnum(McLanguage.NL_NL, ExampleMessage::class.java)

        languageService.reloadAll()
    }


}
