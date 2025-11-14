package online.veloraplugins.base.core.language

/**
 * Represents a message entry that can be stored in the language system.
 *
 * @property key Unique database key
 * @property default Default MiniMessage text
 * @property soundName Optional sound identifier ("ENTITY_PLAYER_LEVELUP")
 */
interface BaseMessage {
    val key: String
    val default: String
    val soundName: String?
        get() = null

    val type: MessageType
        get() = MessageType.CHAT
}