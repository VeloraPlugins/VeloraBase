package online.veloraplugins.base.core.language

import kotlinx.coroutines.runBlocking
import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.core.DaoService
import online.veloraplugins.base.core.database.dao.language.LanguageDao
import online.veloraplugins.base.core.database.dao.language.LanguageEntry
import online.veloraplugins.base.core.service.Service
import online.veloraplugins.base.core.service.ServiceInfo

/**
 * LanguageService
 *
 * Handles:
 * - Multi-language message loading
 * - Runtime caching
 * - Enum-based default insertion
 * - Reloading
 * - Cross-language fallback
 * - Per-sender language resolution
 */
@ServiceInfo(
    name = "Languages",
    dependsOn = [DaoService::class]
)
open class LanguageService(
    app: BasePlugin
) : Service(app) {

    /** Automatically resolve dependency */
    private val daoService: DaoService
        get() = plugin.serviceManager.require(DaoService::class)

    /** DAO for DB communication */
    private lateinit var dao: LanguageDao

    /** Cache: languageCode → key → entry */
    private val cache = mutableMapOf<String, MutableMap<String, LanguageEntry>>()

    /** Default fallback language */
    var defaultLanguage = McLanguage.EN_US

    /**
     * Stores:
     *   McLanguage → List<BaseMessage enums>
     */
    private val registeredEnumMap =
        mutableMapOf<McLanguage, MutableList<Class<out BaseMessage>>>()


    /** Platform-specific language resolver (Paper/Velocity/etc) */
    private var languageResolver: ((Any) -> McLanguage?)? = null


    override suspend fun onLoad() {
        super.onLoad()
        dao = daoService.get(LanguageDao::class)
    }


    fun setLanguageResolver(resolver: (Any) -> McLanguage?) {
        this.languageResolver = resolver
    }

    fun resolve(sender: Any): McLanguage {
        return languageResolver?.invoke(sender) ?: defaultLanguage
    }

    fun registerEnum(language: McLanguage, enumClass: Class<out BaseMessage>) {
        registeredEnumMap
            .computeIfAbsent(language) { mutableListOf() }
            .add(enumClass)
    }

    fun registerEnumForAllLanguages(enumClass: Class<out BaseMessage>) {
        McLanguage.entries.forEach { registerEnum(it, enumClass) }
    }

    fun load(enumClass: Class<out BaseMessage>) {
        plugin.scheduler.runAsync {

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

    fun reload(enumClass: Class<out BaseMessage>) {
        plugin.scheduler.runAsync {
            for ((language, enums) in registeredEnumMap) {
                if (enumClass in enums) {
                    reloadEnumForLanguage(language, enumClass)
                }
            }
        }
    }


    private fun reloadEnumForLanguage(language: McLanguage, enumClass: Class<out BaseMessage>) {
        val langCode = language.code

        val existingDb = runBlocking { dao.getAll(langCode) }
        val cacheMap = cache.computeIfAbsent(langCode) { mutableMapOf() }

        // Eerst oude keys verwijderen
        val toRemove = enumClass.enumConstants.map { it.key }.toSet()
        cacheMap.keys.removeAll(toRemove)

        // Reinsert
        for (msg in enumClass.enumConstants) {
            val dbEntry = existingDb[msg.key]

            if (dbEntry != null) {
                cacheMap[msg.key] = dbEntry
            } else {
                val entry = LanguageEntry(
                    language = language,
                    key = msg.key,
                    value = msg.default,
                    type = msg.type,
                    soundName = msg.soundName
                )
                runBlocking { dao.insertOrUpdate(entry) }
                cacheMap[msg.key] = entry

                log("Inserted missing default '${msg.key}' for $langCode")
            }
        }

        log("Reloaded ${enumClass.simpleName} for $langCode")
    }


    fun reloadAll() {
        plugin.scheduler.runAsync {
            cache.clear()

            for ((language, enums) in registeredEnumMap) {
                for (enumClass in enums) {
                    reloadEnumForLanguage(language, enumClass)
                }
            }

            log("Completed full language reload.")
        }
    }

    fun get(message: BaseMessage): String =
        getEntry(message, defaultLanguage).value

    fun get(sender: Any, message: BaseMessage): String {
        val lang = resolve(sender)
        return getEntry(message, lang).value
    }

    fun getEntry(message: BaseMessage, language: McLanguage): LanguageEntry {
        val code = language.code

        cache[code]?.get(message.key)?.let { return it }
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
