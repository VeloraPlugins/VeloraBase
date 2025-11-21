package online.veloraplugins.base.core.language

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.BasePlugin
import online.veloraplugins.base.core.database.core.SchemaService
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
@ServiceInfo("Languages")
open class LanguageService(
    private val app: BasePlugin
) : Service(app) {

    private val schemaService: SchemaService = getOrThrow(SchemaService::class)

    /** DAO for DB communication */
    private lateinit var dao: LanguageDao

    /** Cache: languageCode → key → entry */
    private val cache = mutableMapOf<String, MutableMap<String, LanguageEntry>>()

    /** Default fallback language */
    var defaultLanguage = McLanguage.EN_US

    /**
     * Stores name → enumClass per language.
     *
     * Example:
     * EN_US -> [ExampleMessage::class]
     * NL_NL -> [ExampleMessage::class]
     */
    private val registeredEnumMap =
        mutableMapOf<McLanguage, MutableList<Class<out BaseMessage>>>()


    /** Platform-specific language resolver (Bukkit/Velocity/etc) */
    private var languageResolver: ((Any) -> McLanguage?)? = null

    override suspend fun onLoad() {
        super.onLoad()
        dao = schemaService.register(LanguageDao::class)
    }

    /**
     * Assign a resolver callback that extracts the user's preferred language.
     *
     * Example usage (Paper):
     * languageService.setLanguageResolver { sender ->
     *     if (sender is Player) userService.fromCache(sender.uniqueId)?.lang
     * }
     */
    fun setLanguageResolver(resolver: (Any) -> McLanguage?) {
        this.languageResolver = resolver
    }

    /**
     * Resolves the language for a given sender.
     * If resolver returns null, fallback → defaultLanguage.
     */
    fun resolve(sender: Any): McLanguage {
        return languageResolver?.invoke(sender) ?: defaultLanguage
    }

    /**
     * Register an enum for one specific language.
     */
    fun registerEnum(language: McLanguage, enumClass: Class<out BaseMessage>) {
        registeredEnumMap
            .computeIfAbsent(language) { mutableListOf() }
            .add(enumClass)
    }

    /**
     * Register an enum for ALL languages.
     */
    fun registerEnumForAllLanguages(enumClass: Class<out BaseMessage>) {
        for (lang in McLanguage.entries) {
            registerEnum(lang, enumClass)
        }
    }

    /**
     * Load cached values from DB only for keys belonging to this enum.
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
     * Reload a specific enum for all languages where it is registered.
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
     * Reloads a single enum for one language.
     */
    private fun reloadEnumForLanguage(language: McLanguage, enumClass: Class<out BaseMessage>) {
        app.scheduler.runAsync {
            val langCode = language.code

            val existingDb = dao.getAll(langCode)
            val cacheMap = cache.computeIfAbsent(langCode) { mutableMapOf() }

            // Remove old keys for this enum
            val toRemove = enumClass.enumConstants.map { it.key }.toSet()
            cacheMap.keys.removeAll(toRemove)

            // Re-insert
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
                    dao.insertOrUpdate(entry)
                    cacheMap[msg.key] = entry

                    log("Inserted missing default '${msg.key}' for $langCode")
                }
            }

            log("Reloaded ${enumClass.simpleName} for $langCode")
        }
    }

    /**
     * Reload everything.
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

    /**
     * Get message using ONLY default language.
     * (Used internally)
     */
    fun get(message: BaseMessage): String {
        return getEntry(message, defaultLanguage).value
    }

    /**
     * NEW:
     * Get message using language resolved for sender.
     */
    fun get(sender: Any, message: BaseMessage): String {
        val lang = resolve(sender)
        return getEntry(message, lang).value
    }

    /**
     * Fetch correct entry with proper fallback logic.
     */
    fun getEntry(message: BaseMessage, language: McLanguage = defaultLanguage): LanguageEntry {
        val lang = language.code

        // Exact language entry found?
        cache[lang]?.get(message.key)?.let { return it }

        // Fallback to default language
        cache[defaultLanguage.code]?.get(message.key)?.let { return it }

        // No DB entry -> return default definition
        return LanguageEntry(
            language = language,
            key = message.key,
            value = message.default,
            type = message.type,
            soundName = message.soundName
        )
    }

    /**
     * Apply placeholder {key} → value replacement.
     */
    fun format(message: String, vararg placeholders: Pair<String, String>): String {
        var msg = message
        placeholders.forEach { (k, v) ->
            msg = msg.replace("{$k}", v)
        }
        return msg
    }
}
