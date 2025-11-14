package online.veloraplugins.base.core.database.dao.language

import online.veloraplugins.base.core.language.MessageType
import org.jetbrains.exposed.sql.Table


object LanguageTable : Table("language_messages") {

    val language = varchar("language", 32)
    val key = varchar("key", 128)
    val value = text("value")

    val type = enumeration("type", MessageType::class)
        .default(MessageType.CHAT)

    val sound = varchar("sound", 64).nullable()

    override val primaryKey = PrimaryKey(language, key)
}
