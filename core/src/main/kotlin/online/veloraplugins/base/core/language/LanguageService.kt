package online.veloraplugins.base.core.language

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.core.SchemaService
import online.veloraplugins.base.core.database.dao.language.LanguageDao
import online.veloraplugins.base.core.database.dao.language.LanguageEntry
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.core.service.Service
import kotlin.reflect.KClass

open class LanguageService(
    private val app: BasePlugin,
) : AbstractService(app) {

    override val dependsOn: Set<KClass<out Service>> =
        setOf(SchemaService::class)

    private lateinit var dao: LanguageDao

    /** Cache: language → (key → LanguageEntry) */
    private val cache = mutableMapOf<String, MutableMap<String, LanguageEntry>>()

    /** Default taal (ENG_US) */
    var defaultLanguage = McLanguage.EN_US

    override suspend fun onEnable() {
        val schema = app.serviceManager.require(SchemaService::class)
        dao = schema.getSchema(LanguageDao::class)
    }

    fun loadLanguage(language: McLanguage) =
        loadLanguageById(language.code)

    fun loadLanguageById(language: String) {
        app.scheduler.runAsync {
            val entries = dao.getAll(language)
            cache[language] = entries.toMutableMap()
            log("Loaded ${entries.size} messages for language: $language")
        }
    }

    fun registerDefaults(language: McLanguage, vararg messages: BaseMessage) {
        val lang = language.code

        app.scheduler.runAsync {

            val existing = dao.getAll(lang)

            val cacheMap = cache.computeIfAbsent(lang) {
                existing.toMutableMap()
            }

            for (msg in messages) {
                if (!existing.containsKey(msg.key)) {

                    val entry = LanguageEntry(
                        language = language,
                        key = msg.key,
                        value = msg.default,
                        type = msg.type,
                        soundName = msg.soundName
                    )

                    dao.insertOrUpdate(entry)
                    cacheMap[msg.key] = entry

                    log("Inserted default message '${msg.key}' ($language)")
                }
            }
        }
    }

    fun registerEnum(language: McLanguage, enumClass: Class<out BaseMessage>) {
        registerDefaults(language, *enumClass.enumConstants)
    }

    fun get(message: BaseMessage, language: McLanguage = defaultLanguage): String {
        return getEntry(message, language).value
    }

    fun getEntry(message: BaseMessage, language: McLanguage = defaultLanguage): LanguageEntry {
        val lang = language.code

        cache[lang]?.get(message.key)?.let { return it }
        cache[defaultLanguage.code]?.get(message.key)?.let { return it }

        return LanguageEntry(
            language = language,
            key = message.key,
            value = message.default,
            type = message.type,
            soundName = message.soundName
        )
    }

    fun format(
        message: String,
        vararg placeholders: Pair<String, String>
    ): String {
        var msg = message
        placeholders.forEach { (k, v) ->
            msg = msg.replace("{$k}", v)
        }
        return msg
    }
}
