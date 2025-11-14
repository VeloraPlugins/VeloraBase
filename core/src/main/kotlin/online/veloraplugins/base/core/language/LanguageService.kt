package online.veloraplugins.base.core.language

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.core.SchemaService
import online.veloraplugins.base.core.database.dao.language.LanguageDao
import online.veloraplugins.base.core.database.dao.language.LanguageEntry
import online.veloraplugins.base.core.service.AbstractService
import online.veloraplugins.base.core.service.Service
import kotlin.reflect.KClass

/**
 * LanguageService
 *
 * Handles:
 * - Multi-language message loading
 * - Runtime caching
 * - Enum-based default insertion
 * - Reloads per enum or globally
 * - Fallback across languages
 */
open class LanguageService(
    private val app: BasePlugin,
) : AbstractService(app) {

    override val dependsOn: Set<KClass<out Service>> =
        setOf(SchemaService::class)

    /** DAO for DB communication */
    private lateinit var dao: LanguageDao

    /** Cache: languageCode → key → entry */
    private val cache = mutableMapOf<String, MutableMap<String, LanguageEntry>>()

    /** Default fallback language */
    var defaultLanguage = McLanguage.EN_US

    /**
     * Map of which enums belong to which language.
     *
     * Example:
     *   EN_US -> [ExampleMessage::class.java]
     *   NL_NL -> [ExampleMessage::class.java, DutchMessage::class.java]
     */
    private val registeredEnumMap =
        mutableMapOf<McLanguage, MutableList<Class<out BaseMessage>>>()

    override suspend fun onEnable() {
        val schema = app.serviceManager.require(SchemaService::class)
        dao = schema.getSchema(LanguageDao::class)
    }

    /**
     * Registers an enum class for a specific language.
     */
    fun registerEnum(language: McLanguage, enumClass: Class<out BaseMessage>) {
        registeredEnumMap
            .computeIfAbsent(language) { mutableListOf() }
            .add(enumClass)
    }

    /**
     * Registers one enum class for ALL languages.
     */
    fun registerEnumForAllLanguages(enumClass: Class<out BaseMessage>) {
        for (lang in McLanguage.entries) {
            registerEnum(lang, enumClass)
        }
    }

    /**
     * Loads cached values from DB only for keys belonging to this enum.
     */
    fun load(enumClass: Class<out BaseMessage>) {
        app.scheduler.runAsync {

            for ((language, enums) in registeredEnumMap) {
                if (!enums.contains(enumClass)) continue

                val langCode = language.code
                val dbMap = dao.getAll(langCode)
                val cacheMap = cache.computeIfAbsent(langCode) { mutableMapOf() }

                for (msg in enumClass.enumConstants) {
                    dbMap[msg.key]?.let { entry ->
                        cacheMap[msg.key] = entry
                    }
                }

                log("Loaded enum ${enumClass.simpleName} for $langCode")
            }
        }
    }

    /**
     * Reloads a single enum for all languages where it is registered.
     */
    fun reload(enumClass: Class<out BaseMessage>) {
        app.scheduler.runAsync {

            for ((language, enums) in registeredEnumMap) {
                if (!enums.contains(enumClass)) continue

                reloadEnumForLanguage(language, enumClass)
            }
        }
    }

    /**
     * Reload all keys from this enum for one specific language.
     */
    private fun reloadEnumForLanguage(language: McLanguage, enumClass: Class<out BaseMessage>) {
        app.scheduler.runAsync {
            val langCode = language.code

            val existingDb = dao.getAll(langCode)
            val cacheMap = cache.computeIfAbsent(langCode) { mutableMapOf() }

            // Remove previous values for this enum
            val enumKeys = enumClass.enumConstants.map { it.key }.toSet()
            cacheMap.keys.removeAll(enumKeys)

            // Reload values
            for (msg in enumClass.enumConstants) {
                val dbEntry = existingDb[msg.key]
                if (dbEntry != null) {
                    cacheMap[msg.key] = dbEntry
                } else {
                    // Create missing default entry
                    val entry = LanguageEntry(
                        language = language,
                        key = msg.key,
                        value = msg.default,
                        type = msg.type,
                        soundName = msg.soundName
                    )
                    dao.insertOrUpdate(entry)
                    cacheMap[msg.key] = entry

                    log("Inserted missing default '${msg.key}' for $langCode")
                }
            }

            log("Reloaded ${enumClass.simpleName} for $langCode")
        }
    }

    /**
     * Reloads the entire language system:
     * - Clears cache
     * - Reloads all DB values
     * - Applies missing defaults for all registered enums
     */
    fun reloadAll() {
        app.scheduler.runAsync {

            cache.clear()

            for ((language, enums) in registeredEnumMap) {
                for (enumClass in enums) {
                    reloadEnumForLanguage(language, enumClass)
                }
            }

            log("Completed full language reload.")
        }
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

    fun format(message: String, vararg placeholders: Pair<String, String>): String {
        var msg = message
        placeholders.forEach { (k, v) ->
            msg = msg.replace("{$k}", v)
        }
        return msg
    }
}
