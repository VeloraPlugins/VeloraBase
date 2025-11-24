package online.veloraplugins.mccommon

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import kotlin.math.min


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
     * Fully normalizes any input by processing both legacy (&c)
     * and MiniMessage (<red>) formatting.
     */
    fun parse(input: String, fullReset: Boolean = true): Component {
        val legacyComponent = legacySerializer.deserialize(input)
        val miniString = mini.serialize(legacyComponent)

        val deserialize = mini.deserialize(miniString)

        if (fullReset)
            return Component.empty()
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.STRIKETHROUGH, false)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.OBFUSCATED, false)
                .append(deserialize)
        return mini.deserialize(miniString)
    }
}
