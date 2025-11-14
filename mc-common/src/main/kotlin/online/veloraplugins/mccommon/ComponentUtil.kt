package online.veloraplugins.mccommon

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer


object ComponentUtil {

    private val mini = MiniMessage.builder()
        .tags(StandardTags.defaults())
        .build()

    // Legacy serializer that understands:
    // &0-&f, &k, &l, &m, &n, &o, &r
    // and hex colors like &#RRGGBB and &x&R&R&G&G&B&B
    private val legacySerializer = LegacyComponentSerializer.builder()
        .character('&')
        .hexCharacter('#')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat() // Enables &x&R&R&G&G&B&B
        .build()

    /**
     * Converts legacy (&-codes) into a Component.
     */
    fun fromLegacy(input: String): Component {
        return legacySerializer.deserialize(input)
    }

    /**
     * Converts a Component into legacy (&-codes).
     */
    fun toLegacy(component: Component): String {
        return legacySerializer.serialize(component)
    }

    /**
     * Converts legacy (& codes) → MiniMessage (<color> etc.).
     * Useful for upgrading configs.
     */
    fun legacyToMini(input: String): String {
        val component = legacySerializer.deserialize(input)
        return mini.serialize(component)
    }

    /**
     * Converts MiniMessage → Component
     */
    fun fromMini(input: String): Component {
        return mini.deserialize(input)
    }

    /**
     * Converts Component → MiniMessage
     */
    fun toMini(component: Component): String {
        return mini.serialize(component)
    }

    /**
     * Fully normalizes any string input into a Component.
     * Automatically detects if the string contains MiniMessage or legacy codes.
     */
    fun parse(input: String): Component {
        return when {
            input.contains('<') && input.contains('>') -> fromMini(input)
            else -> fromLegacy(input)
        }
    }
}
