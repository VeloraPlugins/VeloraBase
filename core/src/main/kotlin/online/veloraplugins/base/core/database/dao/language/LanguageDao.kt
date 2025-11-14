package online.veloraplugins.base.core.database.dao.language

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.database.core.DatabaseService
import online.veloraplugins.base.core.database.dao.BaseDao
import org.jetbrains.exposed.sql.*

class LanguageDao(
    db: DatabaseService
) : BaseDao<LanguageTable>(db, LanguageTable) {

    suspend fun get(language: String, key: String): LanguageEntry? =
        findOne {
            (LanguageTable.language eq language) and (LanguageTable.key eq key)
        }?.toEntry()

    suspend fun insertOrUpdate(entry: LanguageEntry) {
        val langId = entry.language.code

        query {
            LanguageTable.insertIgnore {
                it[language] = langId
                it[key] = entry.key
                it[value] = entry.value
                it[type] = entry.type
                it[sound] = entry.soundName
            }

            LanguageTable.update(
                where = {
                    (LanguageTable.language eq langId) and
                            (LanguageTable.key eq entry.key)
                }
            ) {
                it[value] = entry.value
                it[type] = entry.type
                it[sound] = entry.soundName
            }
        }
    }

    suspend fun getAll(language: String): Map<String, LanguageEntry> =
        findMany {
            LanguageTable.language eq language
        }.associate { row ->
            val entry = row.toEntry()
            entry.key to entry
        }

    /**
     * Returns ALL language entries, grouped per language.
     *
     * Output:
     *  {
     *      "en_US" -> { "hello" -> LanguageEntry(...), "bye" -> LanguageEntry(...) },
     *      "nl_NL" -> { "hello" -> LanguageEntry(...), "bye" -> LanguageEntry(...) }
     *  }
     */
    suspend fun getAllLanguages(): Map<String, Map<String, LanguageEntry>> =
        findMany { Op.TRUE } // SELECT * FROM table
            .groupBy { row -> row[LanguageTable.language] } // group by language
            .mapValues { (_, rows) ->
                rows.associate { row ->
                    val entry = row.toEntry()
                    entry.key to entry
                }
            }
}

/** Converts a ResultRow → LanguageEntry */
private fun ResultRow.toEntry(): LanguageEntry {
    val langCode = this[LanguageTable.language]
    val lang = McLanguage.fromCode(langCode) ?: McLanguage.EN_US

    return LanguageEntry(
        language = lang,
        key = this[LanguageTable.key],
        value = this[LanguageTable.value],
        type = this[LanguageTable.type],
        soundName = this[LanguageTable.sound]
    )
}

