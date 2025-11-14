package online.veloraplugins.base.core.database.dao.language

import online.veloraplugins.base.common.enums.McLanguage
import online.veloraplugins.base.core.language.MessageType

data class LanguageEntry(
    val language: McLanguage,
    val key: String,
    val value: String,
    val type: MessageType,
    val soundName: String?
)
