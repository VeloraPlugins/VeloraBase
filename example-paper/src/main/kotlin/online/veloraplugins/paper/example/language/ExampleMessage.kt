package online.veloraplugins.paper.example.language

import online.veloraplugins.base.core.language.BaseMessage

enum class ExampleMessage(
    override val key: String,
    override val default: String,
    override val soundName: String? = null
) : BaseMessage {

    PLAYER_NOT_FOUND(
        key = "player-not-found",
        default = "<red>That player does not exist!</red>",
        soundName ="ENTITY_VILLAGER_NO"
    ),

    NO_PERMISSION(
        key = "no-permission",
        default = "<red>You do not have permission.",
        soundName = "ENTITY_VILLAGER_NO"
    ),

    HELLO_MESSAGE(
        key = "hello-message",
        default = "<green>Hello, {player}!"
    ),

    LEVEL_UP(
        key = "level-up",
        default = "<green>You leveled up!</green>",
        soundName = "ENTITY_PLAYER_LEVELUP"
    );

    companion object {
        /**
         * Returns all messages as an array of BaseMessage (used by registerEnum)
         */
        val ALL: Array<ExampleMessage> = entries.toTypedArray()
    }
}